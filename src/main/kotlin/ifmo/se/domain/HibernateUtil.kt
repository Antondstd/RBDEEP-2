package ifmo.se.domain

import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.service.ServiceRegistry
import org.hibernate.cfg.Configuration

class HibernateUtil {

    companion object {
        var sessionFactory: SessionFactory? = null
            get() {
                if (field == null) {
                    try {
                        val configuration = Configuration()
                        configuration.apply {
                            addAnnotatedClass(MusicComposition::class.java)
                            addAnnotatedClass(UserModel::class.java)
                        }
                        val serviceRegistry: ServiceRegistry = StandardServiceRegistryBuilder()
                            .applySettings(configuration.getProperties()).build()
                        println("Hibernate serviceRegistry created")
                        field = configuration.buildSessionFactory(serviceRegistry)
                        return field
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return field
            }
    }
}