package dev.bandno.decision

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RegionDirectoryTest {
    private val directory = RegionDirectory.load(
        mobileTable = RegionDirectory::class.java.getResourceAsStream("/region-cn-mobile.bin")!!.readBytes(),
        landlineTable = RegionDirectory::class.java.getResourceAsStream("/region-cn-landline.bin")!!.readBytes(),
        countries = CallingCodes.displayNames,
    )

    @Test
    fun describesChinaMobileAndLandline() {
        assertEquals("北京市", directory.describe(domestic("13800138000")))
        assertEquals("上海市", directory.describe(domestic("13900139000")))
        assertEquals("北京市", directory.describe(domestic("01087654321")))
        assertEquals("北京市", directory.describe(domestic("01012345678")))
        assertEquals("广东省深圳市", directory.describe(domestic("075512345678")))
        assertEquals("浙江省杭州市", directory.describe(domestic("057112345678")))
        assertEquals("北京市", directory.describe(international("8613800138000")))
        assertEquals("四川省自贡市", directory.describe(domestic("81312345678")))
        assertEquals("日本", directory.describe(international("81312345678")))
        assertEquals("山东省济南市", directory.describe(domestic("13000000000")))
        assertEquals("江苏省常州市", directory.describe(domestic("13000010000")))
    }

    @Test
    fun describesOtherCountriesWithoutSplittingSharedCodes() {
        assertEquals("日本", directory.describe(international("81312345678")))
        assertEquals("香港", directory.describe(international("85212345678")))
        assertEquals("美国/加拿大", directory.describe(international("14155552671")))
        assertEquals("俄罗斯/哈萨克斯坦", directory.describe(international("77011234567")))
    }

    @Test
    fun unknownNumbersHaveNoRegion() {
        assertNull(directory.describe(null))
        assertNull(directory.describe(domestic("")))
        assertNull(directory.describe(domestic("10086")))
        assertNull(directory.describe(domestic("95588")))
        assertNull(directory.describe(domestic("19900000000")))
        assertNull(directory.describe(domestic("010123456")))
        assertNull(directory.describe(international("86")))
    }
}

private fun domestic(digits: String) = NormalizedNumber(digits, international = false)

private fun international(digits: String) = NormalizedNumber(digits, international = true)
