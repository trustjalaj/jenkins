package com.jalaj.schemapractice.web.rest;

import com.jalaj.schemapractice.JhipsterMySqlApp;
import com.jalaj.schemapractice.domain.Customer;
import com.jalaj.schemapractice.domain.ProductOrder;
import com.jalaj.schemapractice.domain.enumeration.OrderStatus;
import com.jalaj.schemapractice.repository.ProductOrderRepository;
import com.jalaj.schemapractice.service.ProductOrderService;
import com.jalaj.schemapractice.service.dto.ProductOrderDTO;
import com.jalaj.schemapractice.service.mapper.ProductOrderMapper;
import com.jalaj.schemapractice.web.rest.errors.ExceptionTranslator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.jalaj.schemapractice.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/**
 * Test class for the ProductOrderResource REST controller.
 *
 * @see ProductOrderResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JhipsterMySqlApp.class)
@WithMockUser(username="admin", authorities={"ROLE_ADMIN"}, password = "admin")
public class ProductOrderResourceIntTest {

    private static final Instant DEFAULT_PLACED_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_PLACED_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final OrderStatus DEFAULT_STATUS = OrderStatus.COMPLETED;
    private static final OrderStatus UPDATED_STATUS = OrderStatus.PENDING;

    private static final String DEFAULT_CODE = "AAAAAAAAAA";
    private static final String UPDATED_CODE = "BBBBBBBBBB";

    @Autowired
    private ProductOrderRepository productOrderRepository;


    @Autowired
    private ProductOrderMapper productOrderMapper;
    

    @Autowired
    private ProductOrderService productOrderService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restProductOrderMockMvc;

    private ProductOrder productOrder;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ProductOrderResource productOrderResource = new ProductOrderResource(productOrderService);
        this.restProductOrderMockMvc = MockMvcBuilders.standaloneSetup(productOrderResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static ProductOrder createEntity(EntityManager em) {
        ProductOrder productOrder = new ProductOrder()
            .placedDate(DEFAULT_PLACED_DATE)
            .status(DEFAULT_STATUS)
            .code(DEFAULT_CODE);
        // Add required entity
        Customer customer = CustomerResourceIntTest.createEntity(em);
        em.persist(customer);
        em.flush();
        productOrder.setCustomer(customer);
        return productOrder;
    }

    @Before
    public void initTest() {
        productOrder = createEntity(em);
    }

    @Test
    @Transactional
    public void createProductOrder() throws Exception {
        int databaseSizeBeforeCreate = productOrderRepository.findAll().size();

        // Create the ProductOrder
        ProductOrderDTO productOrderDTO = productOrderMapper.toDto(productOrder);
        restProductOrderMockMvc.perform(post("/api/product-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(productOrderDTO)))
            .andExpect(status().isCreated());

        // Validate the ProductOrder in the database
        List<ProductOrder> productOrderList = productOrderRepository.findAll();
        assertThat(productOrderList).hasSize(databaseSizeBeforeCreate + 1);
        ProductOrder testProductOrder = productOrderList.get(productOrderList.size() - 1);
        assertThat(testProductOrder.getPlacedDate()).isEqualTo(DEFAULT_PLACED_DATE);
        assertThat(testProductOrder.getStatus()).isEqualTo(DEFAULT_STATUS);
        assertThat(testProductOrder.getCode()).isEqualTo(DEFAULT_CODE);
    }

    @Test
    @Transactional
    public void createProductOrderWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = productOrderRepository.findAll().size();

        // Create the ProductOrder with an existing ID
        productOrder.setId(1L);
        ProductOrderDTO productOrderDTO = productOrderMapper.toDto(productOrder);

        // An entity with an existing ID cannot be created, so this API call must fail
        restProductOrderMockMvc.perform(post("/api/product-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(productOrderDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ProductOrder in the database
        List<ProductOrder> productOrderList = productOrderRepository.findAll();
        assertThat(productOrderList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkPlacedDateIsRequired() throws Exception {
        int databaseSizeBeforeTest = productOrderRepository.findAll().size();
        // set the field null
        productOrder.setPlacedDate(null);

        // Create the ProductOrder, which fails.
        ProductOrderDTO productOrderDTO = productOrderMapper.toDto(productOrder);

        restProductOrderMockMvc.perform(post("/api/product-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(productOrderDTO)))
            .andExpect(status().isBadRequest());

        List<ProductOrder> productOrderList = productOrderRepository.findAll();
        assertThat(productOrderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkStatusIsRequired() throws Exception {
        int databaseSizeBeforeTest = productOrderRepository.findAll().size();
        // set the field null
        productOrder.setStatus(null);

        // Create the ProductOrder, which fails.
        ProductOrderDTO productOrderDTO = productOrderMapper.toDto(productOrder);

        restProductOrderMockMvc.perform(post("/api/product-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(productOrderDTO)))
            .andExpect(status().isBadRequest());

        List<ProductOrder> productOrderList = productOrderRepository.findAll();
        assertThat(productOrderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCodeIsRequired() throws Exception {
        int databaseSizeBeforeTest = productOrderRepository.findAll().size();
        // set the field null
        productOrder.setCode(null);

        // Create the ProductOrder, which fails.
        ProductOrderDTO productOrderDTO = productOrderMapper.toDto(productOrder);

        restProductOrderMockMvc.perform(post("/api/product-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(productOrderDTO)))
            .andExpect(status().isBadRequest());

        List<ProductOrder> productOrderList = productOrderRepository.findAll();
        assertThat(productOrderList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllProductOrders() throws Exception {
        // Initialize the database
        productOrderRepository.saveAndFlush(productOrder);

        // Get all the productOrderList
        restProductOrderMockMvc.perform(get("/api/product-orders?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(productOrder.getId().intValue())))
            .andExpect(jsonPath("$.[*].placedDate").value(hasItem(DEFAULT_PLACED_DATE.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE.toString())));
    }
    

    @Test
    @Transactional
    public void getProductOrder() throws Exception {
        // Initialize the database
        productOrderRepository.saveAndFlush(productOrder);

        // Get the productOrder
        restProductOrderMockMvc.perform(get("/api/product-orders/{id}", productOrder.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(productOrder.getId().intValue()))
            .andExpect(jsonPath("$.placedDate").value(DEFAULT_PLACED_DATE.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE.toString()));
    }
    @Test
    @Transactional
    public void getNonExistingProductOrder() throws Exception {
        // Get the productOrder
        restProductOrderMockMvc.perform(get("/api/product-orders/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateProductOrder() throws Exception {
        // Initialize the database
        productOrderRepository.saveAndFlush(productOrder);

        int databaseSizeBeforeUpdate = productOrderRepository.findAll().size();

        // Update the productOrder
        ProductOrder updatedProductOrder = productOrderRepository.findById(productOrder.getId()).get();
        // Disconnect from session so that the updates on updatedProductOrder are not directly saved in db
        em.detach(updatedProductOrder);
        updatedProductOrder
            .placedDate(UPDATED_PLACED_DATE)
            .status(UPDATED_STATUS)
            .code(UPDATED_CODE);
        ProductOrderDTO productOrderDTO = productOrderMapper.toDto(updatedProductOrder);

        restProductOrderMockMvc.perform(put("/api/product-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(productOrderDTO)))
            .andExpect(status().isOk());

        // Validate the ProductOrder in the database
        List<ProductOrder> productOrderList = productOrderRepository.findAll();
        assertThat(productOrderList).hasSize(databaseSizeBeforeUpdate);
        ProductOrder testProductOrder = productOrderList.get(productOrderList.size() - 1);
        assertThat(testProductOrder.getPlacedDate()).isEqualTo(UPDATED_PLACED_DATE);
        assertThat(testProductOrder.getStatus()).isEqualTo(UPDATED_STATUS);
        assertThat(testProductOrder.getCode()).isEqualTo(UPDATED_CODE);
    }

    @Test
    @Transactional
    public void updateNonExistingProductOrder() throws Exception {
        int databaseSizeBeforeUpdate = productOrderRepository.findAll().size();

        // Create the ProductOrder
        ProductOrderDTO productOrderDTO = productOrderMapper.toDto(productOrder);

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restProductOrderMockMvc.perform(put("/api/product-orders")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(productOrderDTO)))
            .andExpect(status().isBadRequest());

        // Validate the ProductOrder in the database
        List<ProductOrder> productOrderList = productOrderRepository.findAll();
        assertThat(productOrderList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteProductOrder() throws Exception {
        // Initialize the database
        productOrderRepository.saveAndFlush(productOrder);

        int databaseSizeBeforeDelete = productOrderRepository.findAll().size();

        // Get the productOrder
        restProductOrderMockMvc.perform(delete("/api/product-orders/{id}", productOrder.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<ProductOrder> productOrderList = productOrderRepository.findAll();
        assertThat(productOrderList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProductOrder.class);
        ProductOrder productOrder1 = new ProductOrder();
        productOrder1.setId(1L);
        ProductOrder productOrder2 = new ProductOrder();
        productOrder2.setId(productOrder1.getId());
        assertThat(productOrder1).isEqualTo(productOrder2);
        productOrder2.setId(2L);
        assertThat(productOrder1).isNotEqualTo(productOrder2);
        productOrder1.setId(null);
        assertThat(productOrder1).isNotEqualTo(productOrder2);
    }

    @Test
    @Transactional
    public void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(ProductOrderDTO.class);
        ProductOrderDTO productOrderDTO1 = new ProductOrderDTO();
        productOrderDTO1.setId(1L);
        ProductOrderDTO productOrderDTO2 = new ProductOrderDTO();
        assertThat(productOrderDTO1).isNotEqualTo(productOrderDTO2);
        productOrderDTO2.setId(productOrderDTO1.getId());
        assertThat(productOrderDTO1).isEqualTo(productOrderDTO2);
        productOrderDTO2.setId(2L);
        assertThat(productOrderDTO1).isNotEqualTo(productOrderDTO2);
        productOrderDTO1.setId(null);
        assertThat(productOrderDTO1).isNotEqualTo(productOrderDTO2);
    }

    @Test
    @Transactional
    public void testEntityFromId() {
        assertThat(productOrderMapper.fromId(42L).getId()).isEqualTo(42);
        assertThat(productOrderMapper.fromId(null)).isNull();
    }
}
