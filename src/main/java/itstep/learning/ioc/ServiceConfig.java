package itstep.learning.ioc;

import com.google.inject.AbstractModule;
import itstep.learning.services.config.ConfigService;
import itstep.learning.services.config.JsonConfigService;
import itstep.learning.services.db.DbService;
import itstep.learning.services.db.MySqlDbService;
import itstep.learning.services.form_pars.FormParsService;
import itstep.learning.services.form_pars.MixedFormParsService;
import itstep.learning.services.hash.HashService;
import itstep.learning.services.hash.Md5HashService;
import itstep.learning.services.kdf.KdfService;
import itstep.learning.services.kdf.PbKdf1Service;
import itstep.learning.services.random.RandomService;
import itstep.learning.services.random.UtilRandomService;
import itstep.learning.services.storage.DiskStorageService;
import itstep.learning.services.storage.StorageService;
import itstep.learning.services.time.CurrentTimeService;
import itstep.learning.services.time.TimeService;

public class ServiceConfig extends AbstractModule {
    @Override
    protected void configure() {
       bind(RandomService.class).to(UtilRandomService.class); // AddSingleton<IrRandomService, UtilRandomService>();
       bind(HashService.class).to(Md5HashService.class);
       bind(KdfService.class).to(PbKdf1Service.class);
       bind(DbService.class).to(MySqlDbService.class);
       bind(TimeService.class).to(CurrentTimeService.class);
       bind(ConfigService.class).to(JsonConfigService.class);
       bind(FormParsService.class).to(MixedFormParsService.class);
       bind(StorageService.class).to(DiskStorageService.class);
    }
}
