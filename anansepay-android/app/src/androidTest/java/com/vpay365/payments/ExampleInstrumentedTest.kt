package package im.ananse.payments

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import io.realm.Realm
import io.realm.RealmConfiguration
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    val filename = "testRealm.realm"
    lateinit var realm: Realm
    val currentUserAddress = "rHQCBdcEjCkzEfhAo6DJpqWkHAtrm2ZyKw"

    var testTransactionResultJson: String? = null

    @Before
    fun setup() {

//        testTransactionResultJson = InstrumentationRegistry.getTargetContext().resources.getString(R.string.test_json)

        Realm.init(InstrumentationRegistry.getTargetContext())
        realm = Realm.getInstance(
                RealmConfiguration.Builder()
                        .name(filename)
                        .deleteRealmIfMigrationNeeded()
                        .build())
    }

    @After
    fun tearDown() {

    }

//    @Test
//    fun useAppContext() {
//        // Context of the im.ananse.payments under test.
//        val appContext = InstrumentationRegistry.getTargetContext()
//        assertEquals("com.vpay365.im.ananse.payments", appContext.packageName)
//    }

//    @Test
//    fun persistTransactions() {
//
//        realm.beginTransaction()
//        persistTransactionsLocally(realm, currentUserAddress, JSONObject(testTransactionResultJson))
//        realm.commitTransaction()
//    }

}
