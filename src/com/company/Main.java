package com.company;




import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class Main {
    static List<Ping> pings = new ArrayList<>();
    //NHS,LOUVRE,NASA,AIRCANADA
    static List<Ping> nhs = new ArrayList<>();
    static List<Ping> louvre = new ArrayList<>();
    static List<Ping> nasa = new ArrayList<>();
    static List<Ping> aircanada = new ArrayList<>();


    static List<Trace> traces = new ArrayList<>();
    static List<Trace> tracenhs = new ArrayList<>();
    static List<Trace> tracelouvre = new ArrayList<>();
    static List<Trace> tracenasa = new ArrayList<>();
    static List<Trace> traceaircanada = new ArrayList<>();
    public static Trace record;
    public static List<String> lines;
    public static List<String> listtrace;
    private static  String [] columns={"Destination IP","Packet Transmitted","Packet Received","Loss Rate","Time","Rtt_min","Rtt_avg","Rtt_max","Rtt_mdev"};

    public static void main(String[] args) throws IOException {

        String ping= "routetrace.log";
        String t="t.log";
        String p= "ping.log";
        String trace= "traceroute.log";

        //trParcer(trace);
        //trParcer(trace);
        pingParcer(ping);
        Workbook wk=new XSSFWorkbook();
        Sheet sheet=wk.createSheet();
        Font headerfont=wk.createFont();
        headerfont.setBold(true);
        headerfont.setFontHeightInPoints((short) 17);
        headerfont.setColor(IndexedColors.RED.getIndex());

        CellStyle headerCellStyle=wk.createCellStyle();
        headerCellStyle.setFont(headerfont);

        Row headerRow=sheet.createRow(0);

        for(int i=0;i<columns.length;i++){
            Cell cell=headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }
        int rownum=1;

        for(Ping pg:nhs){
            Row row=sheet.createRow(rownum++);
            row.createCell(0).setCellValue(pg.destination);
            row.createCell(1).setCellValue(pg.packet_transmit);
            row.createCell(2).setCellValue(pg.packet_receive);
            row.createCell(3).setCellValue(pg.packet_loss_rate);
            row.createCell(4).setCellValue(pg.time);
            row.createCell(5).setCellValue(pg.rtt_min);
            row.createCell(6).setCellValue(pg.rtt_avg);
            row.createCell(7).setCellValue(pg.rtt_max);
            row.createCell(8).setCellValue(pg.rtt_mdev);

            for(int i=0;i< columns.length;i++){
                sheet.autoSizeColumn(i);
            }
            FileOutputStream fo=new FileOutputStream("Ping.xlsx");
            wk.write(fo);
            fo.close();
            wk.close();

        }


    }
    public static void Storage(String filename) throws IOException {lines = Files.readAllLines(Paths.get(filename));}


    public static void pingParcer(String filename) throws IOException {
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
            packet_loss_rate=Float.parseFloat(String.valueOf(array.get(3).indexOf(0)));
            time=Integer.parseInt(array.get(4));
            rtt_min=Float.parseFloat(array.get(5));
            rtt_avg=Float.parseFloat(array.get(6));
            rtt_max=Float.parseFloat(array.get(7));
            rtt_mdev=Float.parseFloat(array.get(8));
            Ping p=new Ping(destination,packet_transmit,packet_receive,packet_loss_rate,time,rtt_min,rtt_avg,rtt_max,rtt_mdev);
            pings.add(p);
            array.clear();
        }
        for(int i=0;i<pings.size();i+=4){
            nhs.add(pings.get(i));
        }
        for(int i=1;i<pings.size();i+=4){
            louvre.add(pings.get(i));
        }
        for(int i=2;i<pings.size();i+=4){
            nasa.add(pings.get(i));
        }
        for(int i=4;i<pings.size();i+=4){
            aircanada.add(pings.get(i));
        }

        System.out.println(pings);
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

        System.out.println(traces);
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
    //    int[] Hop;
//    String[] Location;
    ArrayList<String> IPaddress;
    ArrayList<Float> rtt1;
    ArrayList<Float> rtt2;
    ArrayList<Float> rtt3;

    public Trace(String ip, float rtt1, float rtt2, float rtt3) {
    }

    @Override
    public String toString() {
        return "Trace{" +
                "IPaddress='" + IPaddress + '\'' +
                ", rtt1=" + rtt1 +
                ", rtt2=" + rtt2 +
                ", rtt3=" + rtt3 +
                '}';
    }


    // public Trace(int[] Hop, String[] Location, String[] IPaddress, float[] rtt1, float[] rtt2, float[] rtt3) {
    public Trace(ArrayList<String> IPaddress, ArrayList<Float> rtt1, ArrayList<Float> rtt2, ArrayList<Float> rtt3) {
        this.IPaddress = IPaddress;
        this.rtt1 = rtt1;
        this.rtt2 = rtt2;
        this.rtt3 = rtt3;

    }
    public Trace(ArrayList<String> IPaddress) {
        this.IPaddress = IPaddress;
    }

    public Trace() {


    }
}


