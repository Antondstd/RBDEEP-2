package ifmo.se

import ifmo.se.domain.HibernateUtil
import ifmo.se.domain.UserModel
import org.hibernate.Transaction
import javax.persistence.criteria.Predicate

class UserService {
    companion object{

        fun addUser(
            userModel: UserModel
        ): UserModel? {
            var transaction: Transaction? = null
            try {
                val session = HibernateUtil.sessionFactory?.openSession()
                transaction = session?.beginTransaction()
                if (transaction == null || session == null)
                    return null
                session.save(userModel)
                transaction.commit()
                session
                return userModel
            } catch (e: Exception) {
                if (transaction != null)
                    transaction.rollback()
                e.printStackTrace()
                return null
            }
        }

        fun getByLoginAndPassword(author:String,name:String): UserModel?{
            var transaction: Transaction? = null
            try {
                val session = HibernateUtil.sessionFactory?.openSession()
                val criteriaBuilder = session?.criteriaBuilder!!
                val q = criteriaBuilder.createQuery(UserModel::class.java)
                val root = q.from(UserModel::class.java)
                var res = q.select(root)
                val conditions: MutableList<Predicate> = mutableListOf()
                conditions.add(criteriaBuilder.equal(root.get<String>("login"),author))
                conditions.add(criteriaBuilder.equal(root.get<String>("password"),name))
                res.where(criteriaBuilder.and(*conditions.toTypedArray()))
                return session.createQuery(res).singleResult
            } catch (e: Exception) {
                if (transaction != null)
                    transaction.rollback()
                e.printStackTrace()
                return null
            }
        }
    }
}