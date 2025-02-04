package itstep.learning.ioc;

import com.google.inject.AbstractModule;
import itstep.learning.services.db.DbService;
import itstep.learning.services.db.MySqlDbService;
import itstep.learning.services.hash.HashService;
import itstep.learning.services.hash.Md5HashService;
import itstep.learning.services.kdf.KdfService;
import itstep.learning.services.kdf.PbKdf1Service;
import itstep.learning.services.random.RandomService;
import itstep.learning.services.random.UtilRandomService;
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
    }
}
