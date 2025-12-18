// data/database/DatabaseSeeder.kt
package com.example.stroymaterials.data.database

import com.example.stroymaterials.data.database.entities.DeliveryEntity
import com.example.stroymaterials.data.database.entities.MaterialEntity
import com.example.stroymaterials.data.database.entities.SupplierEntity
import com.example.stroymaterials.data.database.entities.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

object DatabaseSeeder {
    /**
     * Инициализирует пользователей при первом запуске приложения
     */
    suspend fun initializeUsers(database: AppDatabase) = withContext(Dispatchers.IO) {
        val userDao = database.userDao()
        
        // Добавляем или обновляем пользователей
        val existingAdmin = userDao.getUserByUsername("admin")
        if (existingAdmin == null) {
            userDao.insert(
                UserEntity(
                    username = "admin",
                    password = "1234",
                    role = "admin",
                    isActive = true
                )
            )
        } else {
            // Обновляем пароль администратора напрямую через SQL
            userDao.updatePassword("admin", "1234")
        }
        
        val existingGuest = userDao.getUserByUsername("guest")
        if (existingGuest == null) {
            userDao.insert(
                UserEntity(
                    username = "guest",
                    password = "guest123",
                    role = "guest",
                    isActive = true
                )
            )
        }
    }
    
    suspend fun seedDatabase(database: AppDatabase) = withContext(Dispatchers.IO) {
        val materialDao = database.materialDao()
        val supplierDao = database.supplierDao()
        val deliveryDao = database.deliveryDao()
        val userDao = database.userDao()

        // Очищаем базу данных
        deliveryDao.deleteAll()
        materialDao.deleteAll()
        supplierDao.deleteAll()
        
        // Инициализируем пользователей
        initializeUsers(database)

        // Создаем поставщиков
        val suppliers = listOf(
            SupplierEntity(
                name = "ООО 'Стройматериалы Плюс'",
                contactPerson = "Иван Петров",
                phone = "+7 (495) 123-45-67",
                email = "info@stroymat-plus.ru",
                address = "ул. Строителей, д. 15",
                city = "Москва",
                rating = 5,
                deliveryTimeDays = 3,
                paymentTerms = "Оплата по факту поставки",
                notes = "Надежный поставщик с 2010 года. Отличное качество материалов.",
                isActive = true
            ),
            SupplierEntity(
                name = "ЗАО 'Стройпоставка'",
                contactPerson = "Петр Сидоров",
                phone = "+7 (812) 987-65-43",
                email = "sales@stroypostavka.ru",
                address = "пр. Инженерный, д. 42",
                city = "Санкт-Петербург",
                rating = 4,
                deliveryTimeDays = 5,
                paymentTerms = "Предоплата 50%",
                notes = "Быстрая доставка. Широкий ассортимент.",
                isActive = true
            ),
            SupplierEntity(
                name = "ИП Ковалев",
                contactPerson = "Алексей Ковалев",
                phone = "+7 (383) 555-12-34",
                email = "kovalev@mail.ru",
                address = "ул. Ленина, д. 25",
                city = "Новосибирск",
                rating = 4,
                deliveryTimeDays = 7,
                paymentTerms = "Оплата наличными при получении",
                notes = "Мелкий опт. Хорошие цены.",
                isActive = true
            ),
            SupplierEntity(
                name = "ООО 'Цемент-Строй'",
                contactPerson = "Мария Иванова",
                phone = "+7 (495) 234-56-78",
                email = "cement@mail.ru",
                address = "ул. Промышленная, д. 8",
                city = "Москва",
                rating = 5,
                deliveryTimeDays = 2,
                paymentTerms = "Оплата по факту",
                notes = "Специализация на цементе и бетоне.",
                isActive = true
            ),
            SupplierEntity(
                name = "ООО 'Кирпич-Торг'",
                contactPerson = "Сергей Волков",
                phone = "+7 (495) 345-67-89",
                email = "kirpich@mail.ru",
                address = "ул. Кирпичная, д. 12",
                city = "Москва",
                rating = 4,
                deliveryTimeDays = 4,
                paymentTerms = "Предоплата 30%",
                notes = "Большой выбор кирпича различных марок.",
                isActive = true
            )
        )

        val supplierIds = suppliers.map { supplier ->
            supplierDao.insert(supplier)
        }

        // Создаем материалы
        val materials = listOf(
            MaterialEntity(
                name = "Цемент М500",
                type = "цемент",
                unit = "кг",
                quantity = 5000.0,
                price = 45.0,
                supplierId = supplierIds[3],
                minStockLevel = 1000.0,
                maxStockLevel = 10000.0,
                warehouseLocation = "Склад А, секция 1",
                description = "Портландцемент марки М500. Высокая прочность.",
                isActive = true
            ),
            MaterialEntity(
                name = "Песок речной",
                type = "песок",
                unit = "т",
                quantity = 25.0,
                price = 1200.0,
                supplierId = supplierIds[0],
                minStockLevel = 10.0,
                maxStockLevel = 50.0,
                warehouseLocation = "Склад Б, открытая площадка",
                description = "Речной песок крупной фракции. Для бетона и штукатурки.",
                isActive = true
            ),
            MaterialEntity(
                name = "Кирпич красный полнотелый",
                type = "кирпич",
                unit = "шт",
                quantity = 15000.0,
                price = 12.5,
                supplierId = supplierIds[4],
                minStockLevel = 5000.0,
                maxStockLevel = 30000.0,
                warehouseLocation = "Склад В, секция 3",
                description = "Керамический кирпич полнотелый. Размер 250x120x65 мм.",
                isActive = true
            ),
            MaterialEntity(
                name = "Арматура А12",
                type = "арматура",
                unit = "т",
                quantity = 8.5,
                price = 45000.0,
                supplierId = supplierIds[1],
                minStockLevel = 2.0,
                maxStockLevel = 20.0,
                warehouseLocation = "Склад Г, секция 2",
                description = "Арматура периодического профиля диаметром 12 мм.",
                isActive = true
            ),
            MaterialEntity(
                name = "Доска обрезная 50x150",
                type = "дерево",
                unit = "м³",
                quantity = 15.0,
                price = 18000.0,
                supplierId = supplierIds[2],
                minStockLevel = 5.0,
                maxStockLevel = 30.0,
                warehouseLocation = "Склад Д, секция 4",
                description = "Доска обрезная хвойных пород. Влажность до 20%.",
                isActive = true
            ),
            MaterialEntity(
                name = "Щебень гранитный 20-40",
                type = "щебень",
                unit = "т",
                quantity = 40.0,
                price = 1500.0,
                supplierId = supplierIds[0],
                minStockLevel = 15.0,
                maxStockLevel = 80.0,
                warehouseLocation = "Склад Б, открытая площадка",
                description = "Гранитный щебень фракции 20-40 мм. Для бетона.",
                isActive = true
            ),
            MaterialEntity(
                name = "Краска акриловая белая",
                type = "краска",
                unit = "л",
                quantity = 120.0,
                price = 450.0,
                supplierId = supplierIds[1],
                minStockLevel = 30.0,
                maxStockLevel = 200.0,
                warehouseLocation = "Склад А, секция 5",
                description = "Акриловая краска для внутренних работ. Белый цвет.",
                isActive = true
            ),
            MaterialEntity(
                name = "Гипсокартон ГКЛ 12.5 мм",
                type = "гипсокартон",
                unit = "м²",
                quantity = 500.0,
                price = 280.0,
                supplierId = supplierIds[2],
                minStockLevel = 100.0,
                maxStockLevel = 1000.0,
                warehouseLocation = "Склад В, секция 6",
                description = "Гипсокартон стандартный толщиной 12.5 мм. Размер 2500x1200 мм.",
                isActive = true
            ),
            MaterialEntity(
                name = "Рубероид РКК-350",
                type = "рубероид",
                unit = "м²",
                quantity = 800.0,
                price = 120.0,
                supplierId = supplierIds[0],
                minStockLevel = 200.0,
                maxStockLevel = 1500.0,
                warehouseLocation = "Склад Г, секция 7",
                description = "Рубероид кровельный наплавляемый. Плотность 350 г/м².",
                isActive = true
            ),
            MaterialEntity(
                name = "Блоки газобетонные 600x300x200",
                type = "блоки",
                unit = "м³",
                quantity = 30.0,
                price = 4200.0,
                supplierId = supplierIds[3],
                minStockLevel = 10.0,
                maxStockLevel = 60.0,
                warehouseLocation = "Склад Д, секция 8",
                description = "Газобетонные блоки для кладки стен. Плотность D600.",
                isActive = true
            )
        )

        val materialIds = materials.map { material ->
            materialDao.insert(material)
        }

        // Создаем поставки
        val calendar = Calendar.getInstance()
        val deliveries = listOf(
            DeliveryEntity(
                materialId = materialIds[0],
                supplierId = supplierIds[3],
                quantity = 2000.0,
                deliveryDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, -15)
                }.time,
                expectedDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, -17)
                }.time,
                status = "delivered",
                invoiceNumber = "INV-2024-001",
                totalCost = 90000.0,
                notes = "Доставка прошла успешно. Все материалы в отличном состоянии."
            ),
            DeliveryEntity(
                materialId = materialIds[1],
                supplierId = supplierIds[0],
                quantity = 10.0,
                deliveryDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, -10)
                }.time,
                expectedDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, -12)
                }.time,
                status = "delivered",
                invoiceNumber = "INV-2024-002",
                totalCost = 12000.0,
                notes = "Песок доставлен в срок."
            ),
            DeliveryEntity(
                materialId = materialIds[2],
                supplierId = supplierIds[4],
                quantity = 5000.0,
                deliveryDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, -5)
                }.time,
                expectedDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, -7)
                }.time,
                status = "delivered",
                invoiceNumber = "INV-2024-003",
                totalCost = 62500.0,
                notes = "Кирпич доставлен. Небольшой бой в пределах нормы."
            ),
            DeliveryEntity(
                materialId = materialIds[3],
                supplierId = supplierIds[1],
                quantity = 5.0,
                deliveryDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, 3)
                }.time,
                expectedDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, 3)
                }.time,
                status = "pending",
                invoiceNumber = "INV-2024-004",
                totalCost = 225000.0,
                notes = "Ожидается поставка арматуры в течение 3 дней."
            ),
            DeliveryEntity(
                materialId = materialIds[4],
                supplierId = supplierIds[2],
                quantity = 8.0,
                deliveryDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, 5)
                }.time,
                expectedDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, 5)
                }.time,
                status = "in_transit",
                invoiceNumber = "INV-2024-005",
                totalCost = 144000.0,
                notes = "Доска в пути. Ожидается доставка через 2 дня."
            ),
            DeliveryEntity(
                materialId = materialIds[5],
                supplierId = supplierIds[0],
                quantity = 20.0,
                deliveryDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, -3)
                }.time,
                expectedDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, -5)
                }.time,
                status = "delivered",
                invoiceNumber = "INV-2024-006",
                totalCost = 30000.0,
                notes = "Щебень доставлен. Качество отличное."
            ),
            DeliveryEntity(
                materialId = materialIds[6],
                supplierId = supplierIds[1],
                quantity = 50.0,
                deliveryDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, -1)
                }.time,
                expectedDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, -2)
                }.time,
                status = "delivered",
                invoiceNumber = "INV-2024-007",
                totalCost = 22500.0,
                notes = "Краска доставлена."
            ),
            DeliveryEntity(
                materialId = materialIds[7],
                supplierId = supplierIds[2],
                quantity = 200.0,
                deliveryDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, 7)
                }.time,
                expectedDate = calendar.apply {
                    add(Calendar.DAY_OF_MONTH, 7)
                }.time,
                status = "pending",
                invoiceNumber = "INV-2024-008",
                totalCost = 56000.0,
                notes = "Ожидается поставка гипсокартона."
            )
        )

        deliveries.forEach { delivery ->
            deliveryDao.insert(delivery)
        }
    }
}

