package com.example.vfsgm.data.constants


enum class Nationality(
    val id: Int,
    val displayName: String,
    val isoCode: String
) {

    AFGHANISTAN(62, "AFGHANISTAN", "AFG"),
    ALBANIA(63, "ALBANIA", "ALB"),
    ALGERIA(24, "ALGERIA", "DZA"),
    ANGOLA(17, "ANGOLA", "AGO"),
    ANGUILLA(65, "ANGUILLA", "AIA"),
    ANTIGUA_AND_BARBUDA(66, "ANTIGUA AND BARBUDA", "ATG"),
    ARGENTINA(67, "ARGENTINA", "ARG"),
    ARMENIA(68, "ARMENIA", "ARM"),
    ARUBA(441, "ARUBA", "ABW"),
    AUSTRALIA(3, "AUSTRALIA", "AUS"),
    AUSTRIA(69, "AUSTRIA", "AUT"),
    AZERBAIJAN(49, "AZERBAIJAN", "AZE"),

    BANGLADESH(11, "BANGLADESH", "BGD"),
    INDIA(46, "INDIA", "IND"),
    PAKISTAN(44, "PAKISTAN", "PAK"),
    UNITED_ARAB_EMIRATES(212, "UNITED ARAB EMIRATES", "ARE"),
    UNITED_KINGDOM(48, "UNITED KINGDOM", "GBR"),
    UNITED_STATES(213, "UNITED STATES", "USA"),
    SRI_LANKA(4, "SRI LANKA", "LKA"),

    // ⛔ always keep this last
    UNKNOWN(-1, "UNKNOWN", "UNK");

    companion object {
        fun fromId(id: Int): Nationality =
            entries.firstOrNull { it.id == id } ?: UNKNOWN

        fun fromIso(isoCode: String): Nationality =
            entries.firstOrNull { it.isoCode.equals(isoCode, ignoreCase = true) } ?: UNKNOWN
    }
}


//const val Countries = [
//    {
//        "id": 62,
//        "masterId": 0,
//        "nationalityName": "AFGHANISTAN",
//        "cultureCode": "en-US",
//        "isoCode": "AFG",
//        "error": null
//    },
//    {
//        "id": 63,
//        "masterId": 0,
//        "nationalityName": "ALBANIA",
//        "cultureCode": "en-US",
//        "isoCode": "ALB",
//        "error": null
//    },
//    {
//        "id": 24,
//        "masterId": 0,
//        "nationalityName": "ALGERIA",
//        "cultureCode": "en-US",
//        "isoCode": "DZA",
//        "error": null
//    },
//    {
//        "id": 17,
//        "masterId": 0,
//        "nationalityName": "ANGOLA",
//        "cultureCode": "en-US",
//        "isoCode": "AGO",
//        "error": null
//    },
//    {
//        "id": 65,
//        "masterId": 0,
//        "nationalityName": "ANGUILLA",
//        "cultureCode": "en-US",
//        "isoCode": "AIA",
//        "error": null
//    },
//    {
//        "id": 66,
//        "masterId": 0,
//        "nationalityName": "ANTIGUA AND BARBUDA",
//        "cultureCode": "en-US",
//        "isoCode": "ATG",
//        "error": null
//    },
//    {
//        "id": 67,
//        "masterId": 0,
//        "nationalityName": "ARGENTINA",
//        "cultureCode": "en-US",
//        "isoCode": "ARG",
//        "error": null
//    },
//    {
//        "id": 68,
//        "masterId": 0,
//        "nationalityName": "ARMENIA",
//        "cultureCode": "en-US",
//        "isoCode": "ARM",
//        "error": null
//    },
//    {
//        "id": 441,
//        "masterId": 0,
//        "nationalityName": "ARUBA",
//        "cultureCode": "en-US",
//        "isoCode": "ABW",
//        "error": null
//    },
//    {
//        "id": 3,
//        "masterId": 0,
//        "nationalityName": "AUSTRALIA",
//        "cultureCode": "en-US",
//        "isoCode": "AUS",
//        "error": null
//    },
//    {
//        "id": 69,
//        "masterId": 0,
//        "nationalityName": "AUSTRIA",
//        "cultureCode": "en-US",
//        "isoCode": "AUT",
//        "error": null
//    },
//    {
//        "id": 49,
//        "masterId": 0,
//        "nationalityName": "AZERBAIJAN",
//        "cultureCode": "en-US",
//        "isoCode": "AZE",
//        "error": null
//    },
//    {
//        "id": 70,
//        "masterId": 0,
//        "nationalityName": "BAHAMAS",
//        "cultureCode": "en-US",
//        "isoCode": "BHS",
//        "error": null
//    },
//    {
//        "id": 19,
//        "masterId": 0,
//        "nationalityName": "BAHRAIN",
//        "cultureCode": "en-US",
//        "isoCode": "BHR",
//        "error": null
//    },
//    {
//        "id": 11,
//        "masterId": 0,
//        "nationalityName": "BANGLADESH",
//        "cultureCode": "en-US",
//        "isoCode": "BGD",
//        "error": null
//    },
//    {
//        "id": 71,
//        "masterId": 0,
//        "nationalityName": "BARBADOS",
//        "cultureCode": "en-US",
//        "isoCode": "BRB",
//        "error": null
//    },
//    {
//        "id": 72,
//        "masterId": 0,
//        "nationalityName": "BELARUS",
//        "cultureCode": "en-US",
//        "isoCode": "BLR",
//        "error": null
//    },
//    {
//        "id": 73,
//        "masterId": 0,
//        "nationalityName": "BELGIUM",
//        "cultureCode": "en-US",
//        "isoCode": "BEL",
//        "error": null
//    },
//    {
//        "id": 74,
//        "masterId": 0,
//        "nationalityName": "BELIZE",
//        "cultureCode": "en-US",
//        "isoCode": "BLZ",
//        "error": null
//    },
//    {
//        "id": 75,
//        "masterId": 0,
//        "nationalityName": "BENIN",
//        "cultureCode": "en-US",
//        "isoCode": "BEN",
//        "error": null
//    },
//    {
//        "id": 4744,
//        "masterId": 0,
//        "nationalityName": "BENIN (DAHOMEY)",
//        "cultureCode": "en-US",
//        "isoCode": "XYM",
//        "error": null
//    },
//    {
//        "id": 76,
//        "masterId": 0,
//        "nationalityName": "BERMUDA",
//        "cultureCode": "en-US",
//        "isoCode": "BMU",
//        "error": null
//    },
//    {
//        "id": 40,
//        "masterId": 0,
//        "nationalityName": "BHUTAN",
//        "cultureCode": "en-US",
//        "isoCode": "BTN",
//        "error": null
//    },
//    {
//        "id": 41,
//        "masterId": 0,
//        "nationalityName": "BOLIVIA",
//        "cultureCode": "en-US",
//        "isoCode": "BOL",
//        "error": null
//    },
//    {
//        "id": 77,
//        "masterId": 0,
//        "nationalityName": "BOSNIA AND HERZEGOVINA",
//        "cultureCode": "en-US",
//        "isoCode": "BIH",
//        "error": null
//    },
//    {
//        "id": 78,
//        "masterId": 0,
//        "nationalityName": "BOTSWANA",
//        "cultureCode": "en-US",
//        "isoCode": "BWA",
//        "error": null
//    },
//    {
//        "id": 9,
//        "masterId": 0,
//        "nationalityName": "BRAZIL",
//        "cultureCode": "en-US",
//        "isoCode": "BRA",
//        "error": null
//    },
//    {
//        "id": 79,
//        "masterId": 0,
//        "nationalityName": "BRITISH VIRGIN ISLANDS",
//        "cultureCode": "en-US",
//        "isoCode": "VGB",
//        "error": null
//    },
//    {
//        "id": 80,
//        "masterId": 0,
//        "nationalityName": "BRUNEI DARUSSALAM",
//        "cultureCode": "en-US",
//        "isoCode": "BRN",
//        "error": null
//    },
//    {
//        "id": 81,
//        "masterId": 0,
//        "nationalityName": "BULGARIA",
//        "cultureCode": "en-US",
//        "isoCode": "BGR",
//        "error": null
//    },
//    {
//        "id": 82,
//        "masterId": 0,
//        "nationalityName": "BURKINA FASO",
//        "cultureCode": "en-US",
//        "isoCode": "BFA",
//        "error": null
//    },
//    {
//        "id": 4745,
//        "masterId": 0,
//        "nationalityName": "BURKINA FASO (UPPER VOLTA)",
//        "cultureCode": "en-US",
//        "isoCode": "XYZ",
//        "error": null
//    },
//    {
//        "id": 83,
//        "masterId": 0,
//        "nationalityName": "BURUNDI",
//        "cultureCode": "en-US",
//        "isoCode": "BDI",
//        "error": null
//    },
//    {
//        "id": 84,
//        "masterId": 0,
//        "nationalityName": "CAMBODIA",
//        "cultureCode": "en-US",
//        "isoCode": "KHM",
//        "error": null
//    },
//    {
//        "id": 4746,
//        "masterId": 0,
//        "nationalityName": "CAMBODIA (KAMPUCHEA)",
//        "cultureCode": "en-US",
//        "isoCode": "XYZ",
//        "error": null
//    },
//    {
//        "id": 85,
//        "masterId": 0,
//        "nationalityName": "CAMEROON",
//        "cultureCode": "en-US",
//        "isoCode": "CMR",
//        "error": null
//    },
//    {
//        "id": 8,
//        "masterId": 0,
//        "nationalityName": "CANADA",
//        "cultureCode": "en-US",
//        "isoCode": "CAN",
//        "error": null
//    },
//    {
//        "id": 86,
//        "masterId": 0,
//        "nationalityName": "CAPE VERDE",
//        "cultureCode": "en-US",
//        "isoCode": "CPV",
//        "error": null
//    },
//    {
//        "id": 442,
//        "masterId": 0,
//        "nationalityName": "CAYMAN ISLANDS",
//        "cultureCode": "en-US",
//        "isoCode": "CYM",
//        "error": null
//    },
//    {
//        "id": 87,
//        "masterId": 0,
//        "nationalityName": "CENTRAL AFRICAN REPUBLIC",
//        "cultureCode": "en-US",
//        "isoCode": "CAF",
//        "error": null
//    },
//    {
//        "id": 88,
//        "masterId": 0,
//        "nationalityName": "CHAD",
//        "cultureCode": "en-US",
//        "isoCode": "TCD",
//        "error": null
//    },
//    {
//        "id": 89,
//        "masterId": 0,
//        "nationalityName": "CHILE",
//        "cultureCode": "en-US",
//        "isoCode": "CHL",
//        "error": null
//    },
//    {
//        "id": 16,
//        "masterId": 0,
//        "nationalityName": "CHINA",
//        "cultureCode": "en-US",
//        "isoCode": "CHN",
//        "error": null
//    },
//    {
//        "id": 443,
//        "masterId": 0,
//        "nationalityName": "CHRISTMAS ISLAND",
//        "cultureCode": "en-US",
//        "isoCode": "CXR",
//        "error": null
//    },
//    {
//        "id": 444,
//        "masterId": 0,
//        "nationalityName": "COCOS (KEELING) ISLANDS",
//        "cultureCode": "en-US",
//        "isoCode": "CCK",
//        "error": null
//    },
//    {
//        "id": 39,
//        "masterId": 0,
//        "nationalityName": "COLOMBIA",
//        "cultureCode": "en-US",
//        "isoCode": "COL",
//        "error": null
//    },
//    {
//        "id": 90,
//        "masterId": 0,
//        "nationalityName": "COMOROS",
//        "cultureCode": "en-US",
//        "isoCode": "COM",
//        "error": null
//    },
//    {
//        "id": 91,
//        "masterId": 0,
//        "nationalityName": "CONGO",
//        "cultureCode": "en-US",
//        "isoCode": "COD",
//        "error": null
//    },
//    {
//        "id": 447,
//        "masterId": 0,
//        "nationalityName": "COOK ISLANDS",
//        "cultureCode": "en-US",
//        "isoCode": "COK",
//        "error": null
//    },
//    {
//        "id": 93,
//        "masterId": 0,
//        "nationalityName": "COSTA RICA",
//        "cultureCode": "en-US",
//        "isoCode": "CRI",
//        "error": null
//    },
//    {
//        "id": 4747,
//        "masterId": 0,
//        "nationalityName": "COTE D'IVOIRE",
//        "cultureCode": "en-US",
//        "isoCode": "XYZ",
//        "error": null
//    },
//    {
//        "id": 95,
//        "masterId": 0,
//        "nationalityName": "CROATIA",
//        "cultureCode": "en-US",
//        "isoCode": "HRV",
//        "error": null
//    },
//    {
//        "id": 4748,
//        "masterId": 0,
//        "nationalityName": "CROTIA",
//        "cultureCode": "en-US",
//        "isoCode": "XYZ",
//        "error": null
//    },
//    {
//        "id": 96,
//        "masterId": 0,
//        "nationalityName": "CUBA",
//        "cultureCode": "en-US",
//        "isoCode": "CUB",
//        "error": null
//    },
//    {
//        "id": 97,
//        "masterId": 0,
//        "nationalityName": "CYPRUS",
//        "cultureCode": "en-US",
//        "isoCode": "CYP",
//        "error": null
//    },
//    {
//        "id": 98,
//        "masterId": 0,
//        "nationalityName": "CZECH REPUBLIC",
//        "cultureCode": "en-US",
//        "isoCode": "CZE",
//        "error": null
//    },
//    {
//        "id": 31,
//        "masterId": 0,
//        "nationalityName": "DEMOCRATIC REPUBLIC OF CONGO",
//        "cultureCode": "en-US",
//        "isoCode": "COD",
//        "error": null
//    },
//    {
//        "id": 100,
//        "masterId": 0,
//        "nationalityName": "DENMARK",
//        "cultureCode": "en-US",
//        "isoCode": "DNK",
//        "error": null
//    },
//    {
//        "id": 101,
//        "masterId": 0,
//        "nationalityName": "DJIBOUTI",
//        "cultureCode": "en-US",
//        "isoCode": "DJI",
//        "error": null
//    },
//    {
//        "id": 102,
//        "masterId": 0,
//        "nationalityName": "DOMINICA",
//        "cultureCode": "en-US",
//        "isoCode": "DMA",
//        "error": null
//    },
//    {
//        "id": 7,
//        "masterId": 0,
//        "nationalityName": "DOMINICAN REPUBLIC",
//        "cultureCode": "en-US",
//        "isoCode": "DOM",
//        "error": null
//    },
//    {
//        "id": 104,
//        "masterId": 0,
//        "nationalityName": "ECUADOR",
//        "cultureCode": "en-US",
//        "isoCode": "ECU",
//        "error": null
//    },
//    {
//        "id": 47,
//        "masterId": 0,
//        "nationalityName": "EGYPT",
//        "cultureCode": "en-US",
//        "isoCode": "EGY",
//        "error": null
//    },
//    {
//        "id": 105,
//        "masterId": 0,
//        "nationalityName": "EL SALVADOR",
//        "cultureCode": "en-US",
//        "isoCode": "SLV",
//        "error": null
//    },
//    {
//        "id": 106,
//        "masterId": 0,
//        "nationalityName": "EQUATORIAL GUINEA",
//        "cultureCode": "en-US",
//        "isoCode": "GNQ",
//        "error": null
//    },
//    {
//        "id": 107,
//        "masterId": 0,
//        "nationalityName": "ERITREA",
//        "cultureCode": "en-US",
//        "isoCode": "ERI",
//        "error": null
//    },
//    {
//        "id": 108,
//        "masterId": 0,
//        "nationalityName": "ESTONIA",
//        "cultureCode": "en-US",
//        "isoCode": "EST",
//        "error": null
//    },
//    {
//        "id": 109,
//        "masterId": 0,
//        "nationalityName": "ETHIOPIA",
//        "cultureCode": "en-US",
//        "isoCode": "ETH",
//        "error": null
//    },
//    {
//        "id": 5341,
//        "masterId": 0,
//        "nationalityName": "Express service",
//        "cultureCode": "en-US",
//        "isoCode": "INX",
//        "error": null
//    },
//    {
//        "id": 448,
//        "masterId": 0,
//        "nationalityName": "FALKLAND ISLANDS",
//        "cultureCode": "en-US",
//        "isoCode": "FLK",
//        "error": null
//    },
//    {
//        "id": 449,
//        "masterId": 0,
//        "nationalityName": "FAROE ISLANDS",
//        "cultureCode": "en-US",
//        "isoCode": "FRO",
//        "error": null
//    },
//    {
//        "id": 51,
//        "masterId": 0,
//        "nationalityName": "FIJI",
//        "cultureCode": "en-US",
//        "isoCode": "FJI",
//        "error": null
//    },
//    {
//        "id": 110,
//        "masterId": 0,
//        "nationalityName": "FINLAND",
//        "cultureCode": "en-US",
//        "isoCode": "FIN",
//        "error": null
//    },
//    {
//        "id": 4763,
//        "masterId": 0,
//        "nationalityName": "FINLAND RESIDENCE PERMIT",
//        "cultureCode": "en-US",
//        "isoCode": "FRP",
//        "error": null
//    },
//    {
//        "id": 111,
//        "masterId": 0,
//        "nationalityName": "FRANCE",
//        "cultureCode": "en-US",
//        "isoCode": "FRA",
//        "error": null
//    },
//    {
//        "id": 112,
//        "masterId": 0,
//        "nationalityName": "GABON",
//        "cultureCode": "en-US",
//        "isoCode": "GAB",
//        "error": null
//    },
//    {
//        "id": 113,
//        "masterId": 0,
//        "nationalityName": "GAMBIA",
//        "cultureCode": "en-US",
//        "isoCode": "GMB",
//        "error": null
//    },
//    {
//        "id": 2,
//        "masterId": 0,
//        "nationalityName": "GEORGIA",
//        "cultureCode": "en-US",
//        "isoCode": "GEO",
//        "error": null
//    },
//    {
//        "id": 114,
//        "masterId": 0,
//        "nationalityName": "GERMANY",
//        "cultureCode": "en-US",
//        "isoCode": "DEU",
//        "error": null
//    },
//    {
//        "id": 50,
//        "masterId": 0,
//        "nationalityName": "GHANA",
//        "cultureCode": "en-US",
//        "isoCode": "GHA",
//        "error": null
//    },
//    {
//        "id": 115,
//        "masterId": 0,
//        "nationalityName": "GIBRALTAR",
//        "cultureCode": "en-US",
//        "isoCode": "GIB",
//        "error": null
//    },
//    {
//        "id": 116,
//        "masterId": 0,
//        "nationalityName": "GREECE",
//        "cultureCode": "en-US",
//        "isoCode": "GRC",
//        "error": null
//    },
//    {
//        "id": 450,
//        "masterId": 0,
//        "nationalityName": "GREENLAND",
//        "cultureCode": "en-US",
//        "isoCode": "GRL",
//        "error": null
//    },
//    {
//        "id": 117,
//        "masterId": 0,
//        "nationalityName": "GRENADA",
//        "cultureCode": "en-US",
//        "isoCode": "GRD",
//        "error": null
//    },
//    {
//        "id": 118,
//        "masterId": 0,
//        "nationalityName": "GUATEMALA",
//        "cultureCode": "en-US",
//        "isoCode": "GTM",
//        "error": null
//    },
//    {
//        "id": 119,
//        "masterId": 0,
//        "nationalityName": "GUINEA",
//        "cultureCode": "en-US",
//        "isoCode": "GIN",
//        "error": null
//    },
//    {
//        "id": 120,
//        "masterId": 0,
//        "nationalityName": "GUINEA-BISSAU",
//        "cultureCode": "en-US",
//        "isoCode": "GNB",
//        "error": null
//    },
//    {
//        "id": 121,
//        "masterId": 0,
//        "nationalityName": "GUYANA",
//        "cultureCode": "en-US",
//        "isoCode": "GUY",
//        "error": null
//    },
//    {
//        "id": 15,
//        "masterId": 0,
//        "nationalityName": "HAITI",
//        "cultureCode": "en-US",
//        "isoCode": "HTI",
//        "error": null
//    },
//    {
//        "id": 451,
//        "masterId": 0,
//        "nationalityName": "HOLY SEE",
//        "cultureCode": "en-US",
//        "isoCode": "VAT",
//        "error": null
//    },
//    {
//        "id": 122,
//        "masterId": 0,
//        "nationalityName": "HONDURAS",
//        "cultureCode": "en-US",
//        "isoCode": "HND",
//        "error": null
//    },
//    {
//        "id": 123,
//        "masterId": 0,
//        "nationalityName": "HONG KONG",
//        "cultureCode": "en-US",
//        "isoCode": "HKG",
//        "error": null
//    },
//    {
//        "id": 5338,
//        "masterId": 0,
//        "nationalityName": "Hong Kong BNO",
//        "cultureCode": "en-US",
//        "isoCode": "GBN",
//        "error": null
//    },
//    {
//        "id": 5337,
//        "masterId": 0,
//        "nationalityName": "Hong Kong SAR",
//        "cultureCode": "en-US",
//        "isoCode": "ZZH",
//        "error": null
//    },
//    {
//        "id": 124,
//        "masterId": 0,
//        "nationalityName": "HUNGARY",
//        "cultureCode": "en-US",
//        "isoCode": "HUN",
//        "error": null
//    },
//    {
//        "id": 125,
//        "masterId": 0,
//        "nationalityName": "ICELAND",
//        "cultureCode": "en-US",
//        "isoCode": "ISL",
//        "error": null
//    },
//    {
//        "id": 46,
//        "masterId": 0,
//        "nationalityName": "INDIA",
//        "cultureCode": "en-US",
//        "isoCode": "IND",
//        "error": null
//    },
//    {
//        "id": 25,
//        "masterId": 0,
//        "nationalityName": "INDONESIA",
//        "cultureCode": "en-US",
//        "isoCode": "IDN",
//        "error": null
//    },
//    {
//        "id": 126,
//        "masterId": 0,
//        "nationalityName": "IRAN",
//        "cultureCode": "en-US",
//        "isoCode": "IRN",
//        "error": null
//    },
//    {
//        "id": 127,
//        "masterId": 0,
//        "nationalityName": "IRAQ",
//        "cultureCode": "en-US",
//        "isoCode": "IRQ",
//        "error": null
//    },
//    {
//        "id": 128,
//        "masterId": 0,
//        "nationalityName": "IRELAND",
//        "cultureCode": "en-US",
//        "isoCode": "IRL",
//        "error": null
//    },
//    {
//        "id": 129,
//        "masterId": 0,
//        "nationalityName": "ISRAEL",
//        "cultureCode": "en-US",
//        "isoCode": "ISR",
//        "error": null
//    },
//    {
//        "id": 130,
//        "masterId": 0,
//        "nationalityName": "ITALY",
//        "cultureCode": "en-US",
//        "isoCode": "ITA",
//        "error": null
//    },
//    {
//        "id": 28,
//        "masterId": 0,
//        "nationalityName": "IVORY COAST",
//        "cultureCode": "en-US",
//        "isoCode": "CIV",
//        "error": null
//    },
//    {
//        "id": 131,
//        "masterId": 0,
//        "nationalityName": "JAMAICA",
//        "cultureCode": "en-US",
//        "isoCode": "JAM",
//        "error": null
//    },
//    {
//        "id": 21,
//        "masterId": 0,
//        "nationalityName": "JAPAN",
//        "cultureCode": "en-US",
//        "isoCode": "JPN",
//        "error": null
//    },
//    {
//        "id": 52,
//        "masterId": 0,
//        "nationalityName": "JORDAN",
//        "cultureCode": "en-US",
//        "isoCode": "JOR",
//        "error": null
//    },
//    {
//        "id": 27,
//        "masterId": 0,
//        "nationalityName": "KAZAKHSTAN",
//        "cultureCode": "en-US",
//        "isoCode": "KAZ",
//        "error": null
//    },
//    {
//        "id": 6,
//        "masterId": 0,
//        "nationalityName": "KENYA ",
//        "cultureCode": "en-US",
//        "isoCode": "KEN",
//        "error": null
//    },
//    {
//        "id": 133,
//        "masterId": 0,
//        "nationalityName": "KIRIBATI",
//        "cultureCode": "en-US",
//        "isoCode": "KIR",
//        "error": null
//    },
//    {
//        "id": 134,
//        "masterId": 0,
//        "nationalityName": "KOREA, DEMOCRATIC PEOPLES REP",
//        "cultureCode": "en-US",
//        "isoCode": "NFK",
//        "error": null
//    },
//    {
//        "id": 455,
//        "masterId": 0,
//        "nationalityName": "KOSOVO",
//        "cultureCode": "en-US",
//        "isoCode": "KOS",
//        "error": null
//    },
//    {
//        "id": 29,
//        "masterId": 0,
//        "nationalityName": "KUWAIT",
//        "cultureCode": "en-US",
//        "isoCode": "KWT",
//        "error": null
//    },
//    {
//        "id": 137,
//        "masterId": 0,
//        "nationalityName": "KYRGYZSTAN",
//        "cultureCode": "en-US",
//        "isoCode": "KGZ",
//        "error": null
//    },
//    {
//        "id": 138,
//        "masterId": 0,
//        "nationalityName": "LAOS",
//        "cultureCode": "en-US",
//        "isoCode": "LAO",
//        "error": null
//    },
//    {
//        "id": 139,
//        "masterId": 0,
//        "nationalityName": "LATVIA",
//        "cultureCode": "en-US",
//        "isoCode": "LVA",
//        "error": null
//    },
//    {
//        "id": 13,
//        "masterId": 0,
//        "nationalityName": "LEBANON",
//        "cultureCode": "en-US",
//        "isoCode": "LBN",
//        "error": null
//    },
//    {
//        "id": 140,
//        "masterId": 0,
//        "nationalityName": "LESOTHO",
//        "cultureCode": "en-US",
//        "isoCode": "LSO",
//        "error": null
//    },
//    {
//        "id": 141,
//        "masterId": 0,
//        "nationalityName": "LIBERIA",
//        "cultureCode": "en-US",
//        "isoCode": "LBR",
//        "error": null
//    },
//    {
//        "id": 61,
//        "masterId": 0,
//        "nationalityName": "LIBYA",
//        "cultureCode": "en-US",
//        "isoCode": "LBY",
//        "error": null
//    },
//    {
//        "id": 4749,
//        "masterId": 0,
//        "nationalityName": "LIBYAN ARAB JAMAHIRIYA",
//        "cultureCode": "en-US",
//        "isoCode": "XYZ",
//        "error": null
//    },
//    {
//        "id": 142,
//        "masterId": 0,
//        "nationalityName": "LIECHTENSTEIN",
//        "cultureCode": "en-US",
//        "isoCode": "LIE",
//        "error": null
//    },
//    {
//        "id": 143,
//        "masterId": 0,
//        "nationalityName": "LITHUANIA",
//        "cultureCode": "en-US",
//        "isoCode": "LTU",
//        "error": null
//    },
//    {
//        "id": 5331,
//        "masterId": 0,
//        "nationalityName": "LITHUANIA RESIDENCE PERMIT",
//        "cultureCode": "en-US",
//        "isoCode": "LTP",
//        "error": null
//    },
//    {
//        "id": 144,
//        "masterId": 0,
//        "nationalityName": "LUXEMBOURG",
//        "cultureCode": "en-US",
//        "isoCode": "LUX",
//        "error": null
//    },
//    {
//        "id": 5339,
//        "masterId": 0,
//        "nationalityName": "Macao Travel Permit",
//        "cultureCode": "en-US",
//        "isoCode": "ZZM",
//        "error": null
//    },
//    {
//        "id": 457,
//        "masterId": 0,
//        "nationalityName": "MACAU",
//        "cultureCode": "en-US",
//        "isoCode": "MAC",
//        "error": null
//    },
//    {
//        "id": 145,
//        "masterId": 0,
//        "nationalityName": "MACEDONIA",
//        "cultureCode": "en-US",
//        "isoCode": "MKD",
//        "error": null
//    },
//    {
//        "id": 146,
//        "masterId": 0,
//        "nationalityName": "MADAGASCAR",
//        "cultureCode": "en-US",
//        "isoCode": "MDG",
//        "error": null
//    },
//    {
//        "id": 34,
//        "masterId": 0,
//        "nationalityName": "MALAWI",
//        "cultureCode": "en-US",
//        "isoCode": "MWI",
//        "error": null
//    },
//    {
//        "id": 33,
//        "masterId": 0,
//        "nationalityName": "MALAYSIA",
//        "cultureCode": "en-US",
//        "isoCode": "MYS",
//        "error": null
//    },
//    {
//        "id": 147,
//        "masterId": 0,
//        "nationalityName": "MALDIVES",
//        "cultureCode": "en-US",
//        "isoCode": "MDV",
//        "error": null
//    },
//    {
//        "id": 148,
//        "masterId": 0,
//        "nationalityName": "MALI",
//        "cultureCode": "en-US",
//        "isoCode": "MLI",
//        "error": null
//    },
//    {
//        "id": 149,
//        "masterId": 0,
//        "nationalityName": "MALTA",
//        "cultureCode": "en-US",
//        "isoCode": "MLT",
//        "error": null
//    },
//    {
//        "id": 5333,
//        "masterId": 0,
//        "nationalityName": "Malta",
//        "cultureCode": "en-US",
//        "isoCode": "MSV",
//        "error": null
//    },
//    {
//        "id": 5334,
//        "masterId": 0,
//        "nationalityName": "Malta",
//        "cultureCode": "en-US",
//        "isoCode": "MFT",
//        "error": null
//    },
//    {
//        "id": 150,
//        "masterId": 0,
//        "nationalityName": "MARSHALL ISLANDS",
//        "cultureCode": "en-US",
//        "isoCode": "MHL",
//        "error": null
//    },
//    {
//        "id": 151,
//        "masterId": 0,
//        "nationalityName": "MAURITANIA",
//        "cultureCode": "en-US",
//        "isoCode": "MRT",
//        "error": null
//    },
//    {
//        "id": 152,
//        "masterId": 0,
//        "nationalityName": "MAURITIUS",
//        "cultureCode": "en-US",
//        "isoCode": "MUS",
//        "error": null
//    },
//    {
//        "id": 153,
//        "masterId": 0,
//        "nationalityName": "MEXICO",
//        "cultureCode": "en-US",
//        "isoCode": "MEX",
//        "error": null
//    },
//    {
//        "id": 154,
//        "masterId": 0,
//        "nationalityName": "MICRONESIA",
//        "cultureCode": "en-US",
//        "isoCode": "FSM",
//        "error": null
//    },
//    {
//        "id": 155,
//        "masterId": 0,
//        "nationalityName": "MOLDOVA",
//        "cultureCode": "en-US",
//        "isoCode": "MDA",
//        "error": null
//    },
//    {
//        "id": 156,
//        "masterId": 0,
//        "nationalityName": "MONACO",
//        "cultureCode": "en-US",
//        "isoCode": "MCO",
//        "error": null
//    },
//    {
//        "id": 157,
//        "masterId": 0,
//        "nationalityName": "MONGOLIA",
//        "cultureCode": "en-US",
//        "isoCode": "MNG",
//        "error": null
//    },
//    {
//        "id": 461,
//        "masterId": 0,
//        "nationalityName": "MONTENEGRO",
//        "cultureCode": "en-US",
//        "isoCode": "MNE",
//        "error": null
//    },
//    {
//        "id": 462,
//        "masterId": 0,
//        "nationalityName": "MONTSERRAT",
//        "cultureCode": "en-US",
//        "isoCode": "MSR",
//        "error": null
//    },
//    {
//        "id": 5,
//        "masterId": 0,
//        "nationalityName": "MOROCCO",
//        "cultureCode": "en-US",
//        "isoCode": "MAR",
//        "error": null
//    },
//    {
//        "id": 158,
//        "masterId": 0,
//        "nationalityName": "MOZAMBIQUE",
//        "cultureCode": "en-US",
//        "isoCode": "MOZ",
//        "error": null
//    },
//    {
//        "id": 463,
//        "masterId": 0,
//        "nationalityName": "MYANMAR, BURMA",
//        "cultureCode": "en-US",
//        "isoCode": "MMR",
//        "error": null
//    },
//    {
//        "id": 160,
//        "masterId": 0,
//        "nationalityName": "NAMIBIA",
//        "cultureCode": "en-US",
//        "isoCode": "NAM",
//        "error": null
//    },
//    {
//        "id": 5342,
//        "masterId": 0,
//        "nationalityName": "National service",
//        "cultureCode": "en-US",
//        "isoCode": "INN",
//        "error": null
//    },
//    {
//        "id": 161,
//        "masterId": 0,
//        "nationalityName": "NAURU",
//        "cultureCode": "en-US",
//        "isoCode": "NRU",
//        "error": null
//    },
//    {
//        "id": 12,
//        "masterId": 0,
//        "nationalityName": "NEPAL",
//        "cultureCode": "en-US",
//        "isoCode": "NPL",
//        "error": null
//    },
//    {
//        "id": 43,
//        "masterId": 0,
//        "nationalityName": "NETHERLANDS",
//        "cultureCode": "en-US",
//        "isoCode": "NLD",
//        "error": null
//    },
//    {
//        "id": 464,
//        "masterId": 0,
//        "nationalityName": "NETHERLANDS ANTILLES",
//        "cultureCode": "en-US",
//        "isoCode": "ANT",
//        "error": null
//    },
//    {
//        "id": 162,
//        "masterId": 0,
//        "nationalityName": "NEW ZEALAND",
//        "cultureCode": "en-US",
//        "isoCode": "NZL",
//        "error": null
//    },
//    {
//        "id": 163,
//        "masterId": 0,
//        "nationalityName": "NICARAGUA",
//        "cultureCode": "en-US",
//        "isoCode": "NIC",
//        "error": null
//    },
//    {
//        "id": 164,
//        "masterId": 0,
//        "nationalityName": "NIGER",
//        "cultureCode": "en-US",
//        "isoCode": "NER",
//        "error": null
//    },
//    {
//        "id": 165,
//        "masterId": 0,
//        "nationalityName": "NIGERIA",
//        "cultureCode": "en-US",
//        "isoCode": "NGA",
//        "error": null
//    },
//    {
//        "id": 166,
//        "masterId": 0,
//        "nationalityName": "NORWAY",
//        "cultureCode": "en-US",
//        "isoCode": "NOR",
//        "error": null
//    },
//    {
//        "id": 22,
//        "masterId": 0,
//        "nationalityName": "OMAN",
//        "cultureCode": "en-US",
//        "isoCode": "OMN",
//        "error": null
//    },
//    {
//        "id": 44,
//        "masterId": 0,
//        "nationalityName": "PAKISTAN",
//        "cultureCode": "en-US",
//        "isoCode": "PAK",
//        "error": null
//    },
//    {
//        "id": 167,
//        "masterId": 0,
//        "nationalityName": "PALAU",
//        "cultureCode": "en-US",
//        "isoCode": "PLW",
//        "error": null
//    },
//    {
//        "id": 4751,
//        "masterId": 0,
//        "nationalityName": "PALESTINE",
//        "cultureCode": "en-US",
//        "isoCode": "PSE",
//        "error": null
//    },
//    {
//        "id": 169,
//        "masterId": 0,
//        "nationalityName": "PANAMA",
//        "cultureCode": "en-US",
//        "isoCode": "PAN",
//        "error": null
//    },
//    {
//        "id": 170,
//        "masterId": 0,
//        "nationalityName": "PAPUA NEW GUINEA",
//        "cultureCode": "en-US",
//        "isoCode": "PNG",
//        "error": null
//    },
//    {
//        "id": 171,
//        "masterId": 0,
//        "nationalityName": "PARAGUAY",
//        "cultureCode": "en-US",
//        "isoCode": "PRY",
//        "error": null
//    },
//    {
//        "id": 172,
//        "masterId": 0,
//        "nationalityName": "PERU",
//        "cultureCode": "en-US",
//        "isoCode": "PER",
//        "error": null
//    },
//    {
//        "id": 54,
//        "masterId": 0,
//        "nationalityName": "PHILIPPINES",
//        "cultureCode": "en-US",
//        "isoCode": "PHL",
//        "error": null
//    },
//    {
//        "id": 466,
//        "masterId": 0,
//        "nationalityName": "PITCAIRN ISLAND",
//        "cultureCode": "en-US",
//        "isoCode": "PCN",
//        "error": null
//    },
//    {
//        "id": 173,
//        "masterId": 0,
//        "nationalityName": "POLAND",
//        "cultureCode": "en-US",
//        "isoCode": "POL",
//        "error": null
//    },
//    {
//        "id": 174,
//        "masterId": 0,
//        "nationalityName": "PORTUGAL",
//        "cultureCode": "en-US",
//        "isoCode": "PRT",
//        "error": null
//    },
//    {
//        "id": 175,
//        "masterId": 0,
//        "nationalityName": "QATAR",
//        "cultureCode": "en-US",
//        "isoCode": "QAT",
//        "error": null
//    },
//    {
//        "id": 4752,
//        "masterId": 0,
//        "nationalityName": "REPUBLIC OF KOREA",
//        "cultureCode": "en-US",
//        "isoCode": "SGS",
//        "error": null
//    },
//    {
//        "id": 4753,
//        "masterId": 0,
//        "nationalityName": "REPUBLIC OF MACEDONIA",
//        "cultureCode": "en-US",
//        "isoCode": "XYZ",
//        "error": null
//    },
//    {
//        "id": 4754,
//        "masterId": 0,
//        "nationalityName": "REPUBLIC OF MOLDOVA",
//        "cultureCode": "en-US",
//        "isoCode": "XYZ",
//        "error": null
//    },
//    {
//        "id": 176,
//        "masterId": 0,
//        "nationalityName": "REPUBLIC OF MONTENEGRO",
//        "cultureCode": "en-US",
//        "isoCode": "MNE",
//        "error": null
//    },
//    {
//        "id": 177,
//        "masterId": 0,
//        "nationalityName": "REPUBLIC OF SERBIA",
//        "cultureCode": "en-US",
//        "isoCode": "SRB",
//        "error": null
//    },
//    {
//        "id": 178,
//        "masterId": 0,
//        "nationalityName": "ROMANIA",
//        "cultureCode": "en-US",
//        "isoCode": "ROU",
//        "error": null
//    },
//    {
//        "id": 179,
//        "masterId": 0,
//        "nationalityName": "RUSSIAN FEDERATION",
//        "cultureCode": "en-US",
//        "isoCode": "RUS",
//        "error": null
//    },
//    {
//        "id": 180,
//        "masterId": 0,
//        "nationalityName": "RWANDA",
//        "cultureCode": "en-US",
//        "isoCode": "RWA",
//        "error": null
//    },
//    {
//        "id": 181,
//        "masterId": 0,
//        "nationalityName": "SAINT KITTS AND NEVIS",
//        "cultureCode": "en-US",
//        "isoCode": "KNA",
//        "error": null
//    },
//    {
//        "id": 182,
//        "masterId": 0,
//        "nationalityName": "SAINT LUCIA",
//        "cultureCode": "en-US",
//        "isoCode": "LCA",
//        "error": null
//    },
//    {
//        "id": 467,
//        "masterId": 0,
//        "nationalityName": "SAINT VINCENT AND THE GRENADINES",
//        "cultureCode": "en-US",
//        "isoCode": "VCT",
//        "error": null
//    },
//    {
//        "id": 184,
//        "masterId": 0,
//        "nationalityName": "SAMOA",
//        "cultureCode": "en-US",
//        "isoCode": "WSM",
//        "error": null
//    },
//    {
//        "id": 185,
//        "masterId": 0,
//        "nationalityName": "SAN MARINO",
//        "cultureCode": "en-US",
//        "isoCode": "SMR",
//        "error": null
//    },
//    {
//        "id": 186,
//        "masterId": 0,
//        "nationalityName": "SAO TOME AND PRINCIPE",
//        "cultureCode": "en-US",
//        "isoCode": "STP",
//        "error": null
//    },
//    {
//        "id": 10,
//        "masterId": 0,
//        "nationalityName": "SAUDI ARABIA",
//        "cultureCode": "en-US",
//        "isoCode": "SAU",
//        "error": null
//    },
//    {
//        "id": 26,
//        "masterId": 0,
//        "nationalityName": "SENEGAL",
//        "cultureCode": "en-US",
//        "isoCode": "SEN",
//        "error": null
//    },
//    {
//        "id": 188,
//        "masterId": 0,
//        "nationalityName": "SEYCHELLES",
//        "cultureCode": "en-US",
//        "isoCode": "SYC",
//        "error": null
//    },
//    {
//        "id": 189,
//        "masterId": 0,
//        "nationalityName": "SIERRA LEONE",
//        "cultureCode": "en-US",
//        "isoCode": "SLE",
//        "error": null
//    },
//    {
//        "id": 14,
//        "masterId": 0,
//        "nationalityName": "SINGAPORE",
//        "cultureCode": "en-US",
//        "isoCode": "SGP",
//        "error": null
//    },
//    {
//        "id": 190,
//        "masterId": 0,
//        "nationalityName": "SLOVAKIA",
//        "cultureCode": "en-US",
//        "isoCode": "SVK",
//        "error": null
//    },
//    {
//        "id": 191,
//        "masterId": 0,
//        "nationalityName": "SLOVENIA",
//        "cultureCode": "en-US",
//        "isoCode": "SVN",
//        "error": null
//    },
//    {
//        "id": 192,
//        "masterId": 0,
//        "nationalityName": "SOLOMON ISLANDS",
//        "cultureCode": "en-US",
//        "isoCode": "SLB",
//        "error": null
//    },
//    {
//        "id": 193,
//        "masterId": 0,
//        "nationalityName": "SOMALIA",
//        "cultureCode": "en-US",
//        "isoCode": "SOM",
//        "error": null
//    },
//    {
//        "id": 45,
//        "masterId": 0,
//        "nationalityName": "SOUTH AFRICA",
//        "cultureCode": "en-US",
//        "isoCode": "ZAF",
//        "error": null
//    },
//    {
//        "id": 20,
//        "masterId": 0,
//        "nationalityName": "SOUTH KOREA",
//        "cultureCode": "en-US",
//        "isoCode": "KOR",
//        "error": null
//    },
//    {
//        "id": 194,
//        "masterId": 0,
//        "nationalityName": "SOUTH SUDAN",
//        "cultureCode": "en-US",
//        "isoCode": "SSD",
//        "error": null
//    },
//    {
//        "id": 195,
//        "masterId": 0,
//        "nationalityName": "SPAIN",
//        "cultureCode": "en-US",
//        "isoCode": "ESP",
//        "error": null
//    },
//    {
//        "id": 4,
//        "masterId": 0,
//        "nationalityName": "SRI LANKA",
//        "cultureCode": "en-US",
//        "isoCode": "LKA",
//        "error": null
//    },
//    {
//        "id": 4755,
//        "masterId": 0,
//        "nationalityName": "St. KITTS & NEVIS",
//        "cultureCode": "en-US",
//        "isoCode": "XYZ",
//        "error": null
//    },
//    {
//        "id": 4739,
//        "masterId": 0,
//        "nationalityName": "STATELESS",
//        "cultureCode": "en-US",
//        "isoCode": "XXA",
//        "error": null
//    },
//    {
//        "id": 196,
//        "masterId": 0,
//        "nationalityName": "SUDAN",
//        "cultureCode": "en-US",
//        "isoCode": "SDN",
//        "error": null
//    },
//    {
//        "id": 4756,
//        "masterId": 0,
//        "nationalityName": "SURINAM",
//        "cultureCode": "en-US",
//        "isoCode": "XYZ",
//        "error": null
//    },
//    {
//        "id": 197,
//        "masterId": 0,
//        "nationalityName": "SURINAME",
//        "cultureCode": "en-US",
//        "isoCode": "SUR",
//        "error": null
//    },
//    {
//        "id": 198,
//        "masterId": 0,
//        "nationalityName": "SWAZILAND",
//        "cultureCode": "en-US",
//        "isoCode": "SWZ",
//        "error": null
//    },
//    {
//        "id": 199,
//        "masterId": 0,
//        "nationalityName": "SWEDEN",
//        "cultureCode": "en-US",
//        "isoCode": "SWE",
//        "error": null
//    },
//    {
//        "id": 200,
//        "masterId": 0,
//        "nationalityName": "SWITZERLAND",
//        "cultureCode": "en-US",
//        "isoCode": "CHE",
//        "error": null
//    },
//    {
//        "id": 60,
//        "masterId": 0,
//        "nationalityName": "SYRIA",
//        "cultureCode": "en-US",
//        "isoCode": "SYR",
//        "error": null
//    },
//    {
//        "id": 470,
//        "masterId": 0,
//        "nationalityName": "Syria, Syrian Arab Republic",
//        "cultureCode": "en-US",
//        "isoCode": "SYR",
//        "error": null
//    },
//    {
//        "id": 4757,
//        "masterId": 0,
//        "nationalityName": "SYRIAN ARAB REPUBLIC",
//        "cultureCode": "en-US",
//        "isoCode": "SYR",
//        "error": null
//    },
//    {
//        "id": 201,
//        "masterId": 0,
//        "nationalityName": "TAIWAN",
//        "cultureCode": "en-US",
//        "isoCode": "TWN",
//        "error": null
//    },
//    {
//        "id": 202,
//        "masterId": 0,
//        "nationalityName": "TAJIKISTAN",
//        "cultureCode": "en-US",
//        "isoCode": "TJK",
//        "error": null
//    },
//    {
//        "id": 203,
//        "masterId": 0,
//        "nationalityName": "TANZANIA",
//        "cultureCode": "en-US",
//        "isoCode": "TZA",
//        "error": null
//    },
//    {
//        "id": 57,
//        "masterId": 0,
//        "nationalityName": "THAILAND",
//        "cultureCode": "en-US",
//        "isoCode": "THA",
//        "error": null
//    },
//    {
//        "id": 4762,
//        "masterId": 0,
//        "nationalityName": "TIBET",
//        "cultureCode": "en-US",
//        "isoCode": "TIB",
//        "error": null
//    },
//    {
//        "id": 473,
//        "masterId": 0,
//        "nationalityName": "TIMOR-LESTE (EAST TIMOR)",
//        "cultureCode": "en-US",
//        "isoCode": "TLS",
//        "error": null
//    },
//    {
//        "id": 206,
//        "masterId": 0,
//        "nationalityName": "TOGO",
//        "cultureCode": "en-US",
//        "isoCode": "TGO",
//        "error": null
//    },
//    {
//        "id": 207,
//        "masterId": 0,
//        "nationalityName": "TONGA",
//        "cultureCode": "en-US",
//        "isoCode": "TON",
//        "error": null
//    },
//    {
//        "id": 208,
//        "masterId": 0,
//        "nationalityName": "TRINIDAD AND TOBAGO",
//        "cultureCode": "en-US",
//        "isoCode": "TTO",
//        "error": null
//    },
//    {
//        "id": 32,
//        "masterId": 0,
//        "nationalityName": "TUNISIA",
//        "cultureCode": "en-US",
//        "isoCode": "TUN",
//        "error": null
//    },
//    {
//        "id": 53,
//        "masterId": 0,
//        "nationalityName": "Turkiye",
//        "cultureCode": "en-US",
//        "isoCode": "TUR",
//        "error": null
//    },
//    {
//        "id": 209,
//        "masterId": 0,
//        "nationalityName": "TURKMENISTAN",
//        "cultureCode": "en-US",
//        "isoCode": "TKM",
//        "error": null
//    },
//    {
//        "id": 474,
//        "masterId": 0,
//        "nationalityName": "TURKS AND CAICOS ISLANDS",
//        "cultureCode": "en-US",
//        "isoCode": "TCA",
//        "error": null
//    },
//    {
//        "id": 210,
//        "masterId": 0,
//        "nationalityName": "TUVALU",
//        "cultureCode": "en-US",
//        "isoCode": "TUV",
//        "error": null
//    },
//    {
//        "id": 211,
//        "masterId": 0,
//        "nationalityName": "UGANDA",
//        "cultureCode": "en-US",
//        "isoCode": "UGA",
//        "error": null
//    },
//    {
//        "id": 4758,
//        "masterId": 0,
//        "nationalityName": "UK BRITISH NATIONAL(OVERSEES)",
//        "cultureCode": "en-US",
//        "isoCode": "XYZ",
//        "error": null
//    },
//    {
//        "id": 4743,
//        "masterId": 0,
//        "nationalityName": "UK BRITISH SUBJECT",
//        "cultureCode": "en-US",
//        "isoCode": "XYZ",
//        "error": null
//    },
//    {
//        "id": 18,
//        "masterId": 0,
//        "nationalityName": "UKRAINE",
//        "cultureCode": "en-US",
//        "isoCode": "UKR",
//        "error": null
//    },
//    {
//        "id": 212,
//        "masterId": 0,
//        "nationalityName": "UNITED ARAB EMIRATES",
//        "cultureCode": "en-US",
//        "isoCode": "ARE",
//        "error": null
//    },
//    {
//        "id": 48,
//        "masterId": 0,
//        "nationalityName": "UNITED KINGDOM",
//        "cultureCode": "en-US",
//        "isoCode": "GBR",
//        "error": null
//    },
//    {
//        "id": 4760,
//        "masterId": 0,
//        "nationalityName": "UNITED NATIONS ORGANIZATION",
//        "cultureCode": "en-US",
//        "isoCode": "XYZ",
//        "error": null
//    },
//    {
//        "id": 213,
//        "masterId": 0,
//        "nationalityName": "UNITED STATES",
//        "cultureCode": "en-US",
//        "isoCode": "USA",
//        "error": null
//    },
//    {
//        "id": 214,
//        "masterId": 0,
//        "nationalityName": "URUGUAY",
//        "cultureCode": "en-US",
//        "isoCode": "URY",
//        "error": null
//    },
//    {
//        "id": 42,
//        "masterId": 0,
//        "nationalityName": "UZBEKISTAN",
//        "cultureCode": "en-US",
//        "isoCode": "UZB",
//        "error": null
//    },
//    {
//        "id": 215,
//        "masterId": 0,
//        "nationalityName": "VANUATU",
//        "cultureCode": "en-US",
//        "isoCode": "VUT",
//        "error": null
//    },
//    {
//        "id": 216,
//        "masterId": 0,
//        "nationalityName": "VATICAN CITY",
//        "cultureCode": "en-US",
//        "isoCode": "VAT",
//        "error": null
//    },
//    {
//        "id": 217,
//        "masterId": 0,
//        "nationalityName": "VENEZUELA",
//        "cultureCode": "en-US",
//        "isoCode": "VEN",
//        "error": null
//    },
//    {
//        "id": 55,
//        "masterId": 0,
//        "nationalityName": "VIETNAM",
//        "cultureCode": "en-US",
//        "isoCode": "VNM",
//        "error": null
//    },
//    {
//        "id": 475,
//        "masterId": 0,
//        "nationalityName": "VIRGIN ISLANDS (BRITISH)",
//        "cultureCode": "en-US",
//        "isoCode": "VGB",
//        "error": null
//    },
//    {
//        "id": 218,
//        "masterId": 0,
//        "nationalityName": "YEMEN",
//        "cultureCode": "en-US",
//        "isoCode": "YEM",
//        "error": null
//    },
//    {
//        "id": 219,
//        "masterId": 0,
//        "nationalityName": "ZAMBIA",
//        "cultureCode": "en-US",
//        "isoCode": "ZMB",
//        "error": null
//    },
//    {
//        "id": 220,
//        "masterId": 0,
//        "nationalityName": "ZIMBABWE",
//        "cultureCode": "en-US",
//        "isoCode": "ZWE",
//        "error": null
//    }
//]