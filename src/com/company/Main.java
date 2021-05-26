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

/**
 * Automated program to visualize and return excel tables with ping information
 */

public class Main {
    static List<Ping> pings = new ArrayList<>(); //Universal List to save all the pings from a given file used for the one week website ping
    static List<Ping> filepings = new ArrayList<>(); // same as before just used to contain the ftp ping result
    /**
     * Custom arraylists used to select records from the "pings" arraylist and associate it to the respected arraylist
     */
    static List<Ping> ws1 = new ArrayList<>();
    static List<Ping> ws2 = new ArrayList<>();
    static List<Ping> ws3 = new ArrayList<>();
    static List<Ping> ws4 = new ArrayList<>();

    static List<List<Ping>> pinglists = new ArrayList<>();//Arraylist of the arraylists containing the lists above

    static List<Trace> traces = new ArrayList<>();//Universal List to save all the traces from a given file used for the one week website traceroute
    /**
     * Custom arraylists used to select records from the "traces" arraylist and associate it to the respected arraylist
     */
    static List<Trace> ips1 = new ArrayList<>();
    static List<Trace> ips2 = new ArrayList<>();
    static List<Trace> ips3 = new ArrayList<>();
    static List<Trace> ips4 = new ArrayList<>();
    /**
     * Custom arraylists used to convert all the ip lists in their respective city names using the database
     */
    static List<CityTrace> citywebsite1 = new ArrayList<>();
    static List<CityTrace> citywebsite2 = new ArrayList<>();
    static List<CityTrace> citywebsite3 = new ArrayList<>();
    static List<CityTrace> citywebsite4 = new ArrayList<>();

    /**
     * Storage containers when reading the files
     */
    public static List<String> lines;
    public static List<String> filelines;
    private static String[] columns = {"Packets Transmitted", "Packets Received", "Loss Rate", "Time", "Min", "Mean", "Max", "Median"};//excel columns

    public static int num=0;
    public static int numberwebsites=4;
    public static int rowindex=0;

    public static void main(String[] args) throws IOException, ParseException, GeoIp2Exception {
        String ping = "newping.log";
        String trace = "newtrace.log";
        String filesping = "3Dlogfile.log";
        TraceParser(trace);
        PingParser(ping);
        FileParser(filesping);
        Excel(pinglists,"pings.xlsx");
        Excel(Collections.singletonList(filepings),"downloadpings.xlsx");
        Cityspopulate();
        System.out.println("Type a number 1-4 to visualize a traceroute of one the four websites:");
        Scanner in = new Scanner(System.in);
        int i = in.nextInt();
        switch (i){
            case 1:
                System.out.println(citywebsite1);
                break;
            case 2:
                System.out.println(citywebsite2);
                break;
            case 3:
                System.out.println(citywebsite3);
                break;
            case 4:
                System.out.println(citywebsite4);
                break;
        }

    }

    /**
     *Fast way to read the log files and store them in the repective arraylists
     */
    public static void Storage(String filename) throws IOException {lines = Files.readAllLines(Paths.get(filename));}
    public static void FileStorage(String filename) throws IOException {filelines = Files.readAllLines(Paths.get(filename));}

    /**
     * Ping parser without arraylist associating to other arraylist
     * @param filename
     * @throws IOException
     * @throws ParseException
     */
    public static void FileParser(String filename) throws IOException, ParseException {
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
        FileStorage(filename);
        ArrayList<String> array=new ArrayList<>();
        String list = String.join("\n", filelines );
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
            Ping p=new Ping(destination,packet_transmit,packet_receive,packet_loss_rate*100,time,rtt_min,rtt_avg,rtt_max,rtt_mdev);
            filepings.add(p);
            array.clear();
        }
    }

    /**
     * Ping parser that divides into 4 arraylists and combines them to one
     * @param filename
     * @throws IOException
     * @throws ParseException
     */
    public static void PingParser(String filename) throws IOException, ParseException {
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
            Ping p=new Ping(destination,packet_transmit,packet_receive,packet_loss_rate*100,time,rtt_min,rtt_avg,rtt_max,rtt_mdev);
            pings.add(p);
            array.clear();
        }
        for(int i=0;i<pings.size();i+=numberwebsites){
            ws1.add(pings.get(i));
        }
        for(int i=1;i<pings.size();i+=numberwebsites){
            ws2.add(pings.get(i));
        }
        for(int i=2;i<pings.size();i+=numberwebsites){
            ws3.add(pings.get(i));
        }
        for(int i=3;i<pings.size();i+=numberwebsites){
            ws4.add(pings.get(i));
        }

        pinglists.add(ws1);
        pinglists.add(ws2);
        pinglists.add(ws3);
        pinglists.add(ws4);
    }

    /**
     * Trace Parser that gets a trace record containing ips and devides them into 4 arraylist so later can be used to be converted from ip addresses to city names
     * @param filename
     * @throws IOException
     */
    public static void TraceParser(String filename) throws IOException {
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
        for(int i=1;i<traces.size();i+=numberwebsites){
            ips1.add(traces.get(i));
        }
        for(int i=2;i<traces.size();i+=numberwebsites){
            ips2.add(traces.get(i));
        }
        for(int i=3;i<traces.size();i+=numberwebsites){
            ips3.add(traces.get(i));
        }
        for(int i=4;i<traces.size();i+=numberwebsites){
            ips4.add(traces.get(i));
        }
    }

    /**
     *Function to generated automatically excel file with ping information giver arraylist of ping result records
     * @param lists
     * @param filename
     * @throws IOException
     */
    public static void Excel(List<List<Ping>> lists,String filename) throws IOException {
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
        FileOutputStream fos=new FileOutputStream(filename);
        num++;
        wk.write(fos);
        fos.close();
        wk.close();

    }

    /**
     * Function to read a given string containing an ip address and convert in into a city name or return a null if its not recognised by the database
     * @param ip
     * @return
     * @throws IOException
     * @throws GeoIp2Exception
     */
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

    /**
     * Function to populate arraylists calling the function above with city names from ip addresses
     * @throws IOException
     * @throws GeoIp2Exception
     */
    public static void Cityspopulate() throws IOException, GeoIp2Exception {
        ArrayList<String>array = new ArrayList<>();
        for(Trace t: ips1){
            for(int i=0;i<t.IPaddress.size();i++){
                array.add((String) Locateip(t.IPaddress.get(i)));
            }
            citywebsite1.add(new CityTrace(array));
            array=new ArrayList<>();
        }
        for(Trace t: ips2){
            for(int i=0;i<t.IPaddress.size();i++){
                array.add((String) Locateip(t.IPaddress.get(i)));
            }
            citywebsite2.add(new CityTrace(array));
            array=new ArrayList<>();
        }
        for(Trace t: ips3){
            for(int i=0;i<t.IPaddress.size();i++){
                array.add((String) Locateip(t.IPaddress.get(i)));
            }
            citywebsite3.add(new CityTrace(array));
            array=new ArrayList<>();
        }
        for(Trace t: ips4){
            for(int i=0;i<t.IPaddress.size();i++){
                array.add((String) Locateip(t.IPaddress.get(i)));
            }
            citywebsite4.add(new CityTrace(array));
            array=new ArrayList<>();
        }
    }
}

/**
 * Ping class to make a custom object so can be used to create a single record with all the information from the ping result
 */
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
}

/**
 * Trace class to create a custom arraylist that returns the ip address in string format
 */
class Trace {
    ArrayList<String> IPaddress;
    public Trace(ArrayList<String> IPaddress) {
        this.IPaddress = IPaddress;
    }
}

/**
 * CityTrace class to create a custom arraylist that returns the City Names in string format
 */
class CityTrace {
    @Override
    public String toString() {
        return "CityName=" + CityName+"\n";
    }
    ArrayList<String> CityName;
    public CityTrace(ArrayList<String> CityName) {
        this.CityName = CityName;
    }
}