package com.company;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class Main {
    //ws1=nhs;ws2=louvre;ws3=nasa;ws4=canada
    static List<Ping> pings = new ArrayList<>();
    static List<Ping> ws1 = new ArrayList<>();
    static List<Ping> ws2 = new ArrayList<>();
    static List<Ping> ws3 = new ArrayList<>();
    static List<Ping> ws4 = new ArrayList<>();

    static List<List<Ping>> pinglists = new ArrayList<>();

    static List<Trace> traces = new ArrayList<>();
    static List<Trace> www1 = new ArrayList<>();
    static List<Trace> www2 = new ArrayList<>();
    static List<Trace> www3 = new ArrayList<>();
    static List<Trace> www4 = new ArrayList<>();

    static List<CityTrace> website1 = new ArrayList<>();
    static List<CityTrace> website2 = new ArrayList<>();
    static List<CityTrace> website3 = new ArrayList<>();
    static List<CityTrace> website4 = new ArrayList<>();

    public static List<String> lines;
    private static String[] columns = {"Packets Transmitted", "Packets Received", "Loss Rate", "Time", "Min", "Mean", "Max", "Median"};

    public static int num=0;
    public static int rowindex=0;

    public static void main(String[] args) throws IOException, ParseException, GeoIp2Exception {
        String ping = "routetrace.log";
        String trace = "t.log";
        trParcer(trace);
        //pingParcer(ping);

        for(int i=0;i<pings.size();i+=4){
            ws1.add(pings.get(i));
        }
        for(int i=1;i<pings.size();i+=4){
            ws2.add(pings.get(i));
        }
        for(int i=2;i<pings.size();i+=4){
            ws3.add(pings.get(i));
        }
        for(int i=3;i<pings.size();i+=4){
            ws4.add(pings.get(i));
        }

        pinglists.add(ws1);
        pinglists.add(ws2);
        pinglists.add(ws3);
        pinglists.add(ws4);
        Excel(pinglists);

        for(int i=1;i<traces.size();i+=4){
            www1.add(traces.get(i));
        }
        for(int i=2;i<traces.size();i+=4){
            www2.add(traces.get(i));
        }
        for(int i=3;i<traces.size();i+=4){
            www3.add(traces.get(i));
        }
        for(int i=4;i<traces.size();i+=4){
            www4.add(traces.get(i));
        }

        Cityspopulate();


    }
    public static void Storage(String filename) throws IOException {lines = Files.readAllLines(Paths.get(filename));}

    public static void pingParcer(String filename) throws IOException, ParseException {
        Pattern pingpattern = Pattern.compile("(\\d+)\\spackets\\stransmitted.*?(\\d+)\\sreceived.*?(\\d+%)\\spacket\\sloss.*?time\\s(\\d+[^a-z]).*?=\\s([^\\/]*)\\/([^\\/]*)\\/([^\\/]*)\\/(.*?)\\sms",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        String destination = null;
        int packet_transmit=0;
        int packet_receive=0;
        float packet_loss_rate=0;
        int time=0;
        float rtt_min=0;
        float rtt_avg=0;
        float rtt_max=0;
        float rtt_mdev=0;
        NumberFormat defaultFormat = NumberFormat.getPercentInstance();
        Storage(filename);
        ArrayList<String> array=new ArrayList<>();
        String list = String.join("\n", lines );
        Matcher m = pingpattern.matcher(list);
        while (m.find()){
            for( int i= 0; i < m.groupCount()+1; i++ ){
                array.add(m.group(i));
            }
            destination=array.get(0);
            packet_transmit=Integer.parseInt(array.get(1));
            packet_receive=Integer.parseInt(array.get(2));
            packet_loss_rate=Float.parseFloat(String.valueOf(defaultFormat.parse(array.get(3))));
            time=Integer.parseInt(array.get(4));
            rtt_min=Float.parseFloat(array.get(5));
            rtt_avg=Float.parseFloat(array.get(6));
            rtt_max=Float.parseFloat(array.get(7));
            rtt_mdev=Float.parseFloat(array.get(8));
            Ping p=new Ping(destination,packet_transmit,packet_receive,packet_loss_rate,time,rtt_min,rtt_avg,rtt_max,rtt_mdev);
            pings.add(p);
            array.clear();
        }


    }

    public static void trParcer(String filename) throws IOException {
        Pattern tracepattern = Pattern.compile("((?:[0-9]{1,3}\\.){3}[0-9]{1,3})",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Pattern traceroute = Pattern.compile("traceroute",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Storage(filename);
        ArrayList<String> array = new ArrayList<>();
        ArrayList<String> ip=new ArrayList<>();
        for (String str : lines) {
            Matcher tracematch = traceroute.matcher(str);
            Matcher m = tracepattern.matcher(str);
            if ((tracematch.find())) {
                traces.add(new Trace(ip));
                ip = new ArrayList<>();
            }
            while (m.find()) {
                for (int i = 0; i < m.groupCount() + 1; i++) {
                    array.add(m.group(i));
                }
                ip.add(array.get(1));
                array.clear();
            }
        }
    }


    public static void Excel(List<List<Ping>> lists) throws IOException {
        Workbook wk = new XSSFWorkbook();
        Sheet sheet = wk.createSheet("Pings");
        Font headerfont = wk.createFont();
        headerfont.setBold(true);
        headerfont.setFontHeightInPoints((short) 15);
        headerfont.setColor(IndexedColors.RED.getIndex());

        CellStyle headerCellStyle = wk.createCellStyle();
        headerCellStyle.setFont(headerfont);

        Row headerRow = sheet.createRow(rowindex);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }
        int rownum = 1;
        Iterator<List<Ping>> iterator=lists.iterator();
        while(iterator.hasNext()){
            for (Ping pg : iterator.next()) {
                Row row = sheet.createRow(rownum++);
                row.createCell(0).setCellValue(pg.packet_transmit);
                row.createCell(1).setCellValue(pg.packet_receive);
                row.createCell(2).setCellValue(pg.packet_loss_rate);
                row.createCell(3).setCellValue(pg.time);
                row.createCell(4).setCellValue(pg.rtt_min);
                row.createCell(5).setCellValue(pg.rtt_avg);
                row.createCell(6).setCellValue(pg.rtt_max);
                row.createCell(7).setCellValue(pg.rtt_mdev);
            }
            Row r = sheet.createRow(rownum);
            rownum++;
            if(iterator.hasNext()) {
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = r.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerCellStyle);
                }
            }
        }
        for(int i=0;i< columns.length;i++){
            sheet.autoSizeColumn(i);
        }
        FileOutputStream fos=new FileOutputStream("doc.xlsx");
        num++;
        wk.write(fos);

        fos.close();
        wk.close();

    }

    public static Object Locateip(String ip) throws IOException, GeoIp2Exception {
        if(ip.contains("10.0.0.1")){
            return "Private IP address";
        }else {
            File database = new File("C:/Users/HMarkov/Documents/geo/GeoLite2-City_20210511/GeoLite2-City.mmdb");
            DatabaseReader reader = new DatabaseReader.Builder(database).build();
            InetAddress ipAddress = InetAddress.getByName(ip);
            CityResponse response = reader.city(ipAddress);
            City city = response.getCity();
            return city.getName();
        }
    }

    public static void Cityspopulate() throws IOException, GeoIp2Exception {
        ArrayList<String>array = new ArrayList<>();
        for(Trace t: www1){
            for(int i=0;i<t.IPaddress.size();i++){
                array.add((String) Locateip(t.IPaddress.get(i)));
            }
            website1.add(new CityTrace(array));
            array=new ArrayList<>();
        }
        for(Trace t: www2){
            for(int i=0;i<t.IPaddress.size();i++){
                array.add((String) Locateip(t.IPaddress.get(i)));
            }
            website2.add(new CityTrace(array));
            array=new ArrayList<>();
        }
        for(Trace t: www3){
            for(int i=0;i<t.IPaddress.size();i++){
                array.add((String) Locateip(t.IPaddress.get(i)));
            }
            website3.add(new CityTrace(array));
            array=new ArrayList<>();
        }
        for(Trace t: www4){
            for(int i=0;i<t.IPaddress.size();i++){
                array.add((String) Locateip(t.IPaddress.get(i)));
            }
            website4.add(new CityTrace(array));
            array=new ArrayList<>();
        }
    }


}
//NHS,LOUVRE,NASA,AIRCANADA
class Ping{

    String destination;
    int packet_transmit;
    int packet_receive;
    float packet_loss_rate;
    int time;
    float rtt_min;
    float rtt_avg;
    float rtt_max;
    float rtt_mdev;

    public Ping(String destination,int packet_transmit,int packet_receive,float packet_loss_rate,int time,float rtt_min,float rtt_avg,float rtt_max,float rtt_mdev){
        this.destination=destination;
        this.packet_transmit=packet_transmit;
        this.packet_receive=packet_receive;
        this.packet_loss_rate=packet_loss_rate;
        this.time=time;
        this.rtt_min=rtt_min;
        this.rtt_avg=rtt_avg;
        this.rtt_max=rtt_max;
        this.rtt_mdev=rtt_mdev;
    }

    @Override
    public String toString() {
        return "Ping{" +
                "destination='" + destination + '\'' +
                ", packet_transmit=" + packet_transmit +
                ", packet_receive=" + packet_receive +
                ", packet_loss_rate=" + packet_loss_rate +
                ", time=" + time +
                ", rtt_min=" + rtt_min +
                ", rtt_avg=" + rtt_avg +
                ", rtt_max=" + rtt_max +
                ", rtt_mdev=" + rtt_mdev +
                '}';
    }
}
class Trace {
    ArrayList<String> IPaddress;
    public Trace(ArrayList<String> IPaddress) {
        this.IPaddress = IPaddress;
    }
}
class CityTrace {
    ArrayList<String> CityName;
    public CityTrace(ArrayList<String> CityName) {
        this.CityName = CityName;
    }
}

