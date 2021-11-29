package ifmo.se

import ifmo.se.domain.HibernateUtil
import ifmo.se.domain.MusicComposition
import org.hibernate.Transaction
import javax.persistence.criteria.Predicate


class MusicService {
    companion object {
        fun addMusicComposition(
            musicComposition: MusicComposition
        ): MusicComposition? {
            var transaction: Transaction? = null
            try {
                val session = HibernateUtil.sessionFactory?.openSession()
                transaction = session?.beginTransaction()
                if (transaction == null || session == null)
                    return null
                session.save(musicComposition)
                transaction.commit()
                session
                return musicComposition
            } catch (e: Exception) {
                if (transaction != null)
                    transaction.rollback()
                e.printStackTrace()
                return null
            }
        }

        fun getAllMusicCompositions(): List<MusicComposition>? {
            var transaction: Transaction? = null
            try {
                val session = HibernateUtil.sessionFactory?.openSession()
                val criteriaBuilder = session?.criteriaBuilder!!
                val q = criteriaBuilder.createQuery(MusicComposition::class.java)
                val root = q.from(MusicComposition::class.java)
                var res = q.select(root)
                return session.createQuery(res).resultList
            } catch (e: Exception) {
                if (transaction != null)
                    transaction.rollback()
                e.printStackTrace()
                return null
            }
        }

        fun getMusicCompositionLike(part:String):List<MusicComposition>?{
            var transaction: Transaction? = null
            try {
                val session = HibernateUtil.sessionFactory?.openSession()
                val criteriaBuilder = session?.criteriaBuilder!!
                val q = criteriaBuilder.createQuery(MusicComposition::class.java)
                val root = q.from(MusicComposition::class.java)
                var res = q.select(root)
                val conditions: MutableList<Predicate> = mutableListOf()
                conditions.add(criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("author")),"%${part.lowercase()}%"))
                conditions.add(criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("name")),"%${part.lowercase()}%"))
                res.where(criteriaBuilder.or(*conditions.toTypedArray()))
                return session.createQuery(res).resultList
            } catch (e: Exception) {
                if (transaction != null)
                    transaction.rollback()
                e.printStackTrace()
                return null
            }
        }

        fun getByAuthorAndName(author:String,name:String):MusicComposition?{
            var transaction: Transaction? = null
            try {
                val session = HibernateUtil.sessionFactory?.openSession()
                val criteriaBuilder = session?.criteriaBuilder!!
                val q = criteriaBuilder.createQuery(MusicComposition::class.java)
                val root = q.from(MusicComposition::class.java)
                var res = q.select(root)
                val conditions: MutableList<Predicate> = mutableListOf()
                conditions.add(criteriaBuilder.equal(root.get<String>("author"),author))
                conditions.add(criteriaBuilder.equal(root.get<String>("name"),name))
                res.where(criteriaBuilder.and(*conditions.toTypedArray()))
                return session.createQuery(res).singleResult
            } catch (e: Exception) {
                if (transaction != null)
                    transaction.rollback()
                e.printStackTrace()
                return null
            }
        }

        fun deleteById(id:Long){
            var transaction: Transaction? = null
            try {
                val session = HibernateUtil.sessionFactory?.openSession()
                transaction = session?.beginTransaction()
                if (transaction == null || session == null)
                    throw Exception("Не удалось создать транзакцию. Нет сессии с базой данных.")
                val ticket = session.find(MusicComposition::class.java, id)
                if (ticket != null) {
                    session.delete(ticket)
                    session.flush()
                    transaction.commit()
                }
                else{
                    transaction.rollback()
                }
            }
            catch (e: Exception) {
                if (transaction != null)
                    transaction.rollback()
                e.printStackTrace()
            }
        }
    }
}