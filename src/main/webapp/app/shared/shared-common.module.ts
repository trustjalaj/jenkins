import { NgModule } from '@angular/core';

import { JhipsterMySqlSharedLibsModule, JhiAlertComponent, JhiAlertErrorComponent } from './';

@NgModule({
    imports: [JhipsterMySqlSharedLibsModule],
    declarations: [JhiAlertComponent, JhiAlertErrorComponent],
    exports: [JhipsterMySqlSharedLibsModule, JhiAlertComponent, JhiAlertErrorComponent]
})
export class JhipsterMySqlSharedCommonModule {}
