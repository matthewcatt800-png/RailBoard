package com.matthew.railboard;

public class Stations {
    public static class Near {
        public final String row;
        public final double meters;
        public Near(String row, double meters) { this.row = row; this.meters = meters; }
    }

    public static final String[] DATA = {
"KGX|London King's Cross|51.5308|-0.1236",
"PAD|London Paddington|51.5152|-0.1755",
"WAT|London Waterloo|51.5047|-0.1135",
"CST|London Cannon Street|51.5108|-0.0927",
"EUS|London Euston|51.5282|-0.1337",
"LBG|London Bridge|51.5049|-0.0857",
"STP|London St Pancras International|51.5314|-0.1263",
"CTK|City Thameslink|51.5133|-0.1013",
"LEW|Lewisham|51.4669|-0.0142",
"BKH|Blackheath|51.4678|0.0067",
"WWA|Woolwich Arsenal|51.4907|0.0699",
"DFD|Dartford|51.4436|0.2208",
"SOO|Strood|51.3940|0.4840",
"CTM|Chatham|51.3800|0.5230",
"RAI|Rainham (Kent)|51.3640|0.5980",
"FAV|Faversham|51.3120|0.7920",
"HNB|Herne Bay|51.3720|1.1260",
"RAM|Ramsgate|51.3480|1.4170",
"DEA|Deal|51.2240|1.4000",
"FKC|Folkestone Central|51.0840|1.1670",
"AFK|Ashford International|51.1370|0.8670",
"CBW|Canterbury West|51.2850|1.0780",
"ORP|Orpington|51.3730|0.0980",
"TON|Tonbridge|51.2220|0.2670",
"SOG|Stonegate|51.0870|0.4160",
"RYE|Rye|50.9520|0.7400",
"AHS|Ashurst|51.1000|0.0810",
"OTF|Otford|51.3070|0.1960",
"SID|Sidcup|51.4260|0.1070",
"HGR|Hither Green|51.4470|0.0070",
"BKJ|Beckenham Junction|51.4060|-0.0380",
"BXY|Bexley|51.4410|0.1530",
"BEX|Bexhill|50.8510|0.4740",
"BTN|Brighton|50.8340|-0.1310",
"LIT|Littlehampton|50.8100|-0.5420",
"PMH|Portsmouth Harbour|50.7940|-1.0990",
"RDH|Redhill|51.2370|-0.1680",
"TBD|Three Bridges|51.1030|-0.1600",
"HRH|Horsham|51.0610|-0.3260",
"RDG|Reading|51.4620|-0.9730",
"CBG|Cambridge|52.1940|0.1340",
"IPS|Ipswich|52.0480|1.2170",
"SIA|Southend Airport|51.5690|0.6860",
"CHM|Chelmsford|51.7340|0.4740",
"MAN|Manchester Piccadilly|53.3770|-2.2300",
"BHM|Birmingham New Street|52.4790|-1.8990",
"LDS|Leeds|53.9530|-1.5470",
"NCL|Newcastle|54.9690|-1.6180",
"DHM|Durham|54.7760|-1.5840",
"EDB|Edinburgh Waverley|55.9500|-3.1910",
"GLQ|Glasgow Queen Street|55.8620|-4.2500",
"CDF|Cardiff Central|51.4840|-3.1830",
"NOT|Nottingham|52.9500|-1.1480",
"DBY|Derby|52.9180|-1.4700",
"HGT|Harrogate|53.9990|-1.5410",
"MBR|Middlesbrough|54.5720|-1.2360",
"LTN|Luton Airport Parkway|51.8770|-0.3770",
"TWI|Twickenham|51.4460|-0.3260",
"EPS|Epsom|51.3330|-0.2710",
"PTR|Petersfield|51.0050|-0.9390",
"PUR|Purley|51.3350|-0.1120",
"HIT|Hitchin|51.9490|-0.2810",
"WGC|Welwyn Garden City|51.7940|-0.1910",
"ELS|Borehamwood|51.6560|-0.2760",
"WIJ|Willesden Junction|51.5250|-0.2330",
"PUT|Putney|51.4640|-0.2100",
"KWB|Kew Bridge|51.4740|-0.2980",
"UPM|Upminster|51.5590|0.2510",
"BRE|Brentwood|51.6170|0.3030",
    };

    public static String crs(String row) { return row.split("\\|")[0]; }
    public static String name(String row) { return row.split("\\|")[1]; }
    public static double lat(String row) { return Double.parseDouble(row.split("\\|")[2]); }
    public static double lon(String row) { return Double.parseDouble(row.split("\\|")[3]); }

    public static double distanceMeters(double la1, double lo1, double la2, double lo2) {
        double R = 6371000;
        double dLat = Math.toRadians(la2 - la1), dLon = Math.toRadians(lo2 - lo1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2) + Math.cos(Math.toRadians(la1))*Math.cos(Math.toRadians(la2))*Math.sin(dLon/2)*Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    public static String nameOf(String crs) {
        for (String row : DATA) if (crs(row).equals(crs)) return name(row);
        return crs;
    }
}
