import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

import { JhipsterMySqlProductModule } from './product/product.module';
import { JhipsterMySqlProductCategoryModule } from './product-category/product-category.module';
import { JhipsterMySqlCustomerModule } from './customer/customer.module';
import { JhipsterMySqlProductOrderModule } from './product-order/product-order.module';
import { JhipsterMySqlOrderItemModule } from './order-item/order-item.module';
import { JhipsterMySqlInvoiceModule } from './invoice/invoice.module';
import { JhipsterMySqlShipmentModule } from './shipment/shipment.module';
/* jhipster-needle-add-entity-module-import - JHipster will add entity modules imports here */

@NgModule({
    // prettier-ignore
    imports: [
        JhipsterMySqlProductModule,
        JhipsterMySqlProductCategoryModule,
        JhipsterMySqlCustomerModule,
        JhipsterMySqlProductOrderModule,
        JhipsterMySqlOrderItemModule,
        JhipsterMySqlInvoiceModule,
        JhipsterMySqlShipmentModule,
        /* jhipster-needle-add-entity-module - JHipster will add entity modules here */
    ],
    declarations: [],
    entryComponents: [],
    providers: [],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class JhipsterMySqlEntityModule {}
