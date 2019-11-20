package com.company;

import sun.rmi.runtime.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

public class Main {

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);                                    //Сканер консоли, ввод

        while (true) {                                                          //Бесконечный цикл работы программы

            File FileInput;                                                     //Файл

            do {                                                                //Ввод имени файла
                try {
                    System.out.print("Enter the file name: " + new java.io.File("").getAbsolutePath());
                    String input = "\\" + in.next();                            //Считывает ввод из консоли
                    FileInput = new File(new java.io.File("").getAbsolutePath() + input);
                } catch (Throwable e) {
                    System.out.println("Error name file long: " + e);
                    FileInput = new File("bad.txt");
                }
            } while (!Exists(FileInput));                                       //Проверка на существование файла

            String InputAnomalyTime;

            do {
                System.out.print("Enter the anomaly time (Enter 'NON' if you not know anomaly time) : ");
                InputAnomalyTime = in.next();                                   //Считывает ввод из консоли

            } while (!InputAnomalyTime.contains("NON") && !isNumeric(InputAnomalyTime));

            int AnomalyTime = 0;
            if(!InputAnomalyTime.contains("NON"))
                AnomalyTime = Integer.parseInt(InputAnomalyTime);               //Аномальное время работы программы

            try (BufferedWriter writer =                                        //Писатель для файла
                         new BufferedWriter(
                                 new OutputStreamWriter(
                                         new FileOutputStream(
                                                 new File("Out.txt")))))
            {

                try (BufferedReader reader = new BufferedReader(                //Читатель для входного файла
                        new FileReader(FileInput))) {

                    LogsIdResultTime[] LogsTime = new LogsIdResultTime[100];
                    int LogsIterator = 0;

                    String LogString = reader.readLine();

                    while (LogString != null){

                        int RequestDataType = 0;                                //Тип данных
                        long StartTime = 0;

                        for(String retval : LogString.split("_")){
                            switch (RequestDataType++){
                                case 0:
                                    SimpleDateFormat Date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    try {
                                        StartTime = Date.parse(retval).getTime()/1000;

                                    } catch (ParseException e){
                                        System.out.println(e.getMessage());
                                    }
                                    break;

                                case 2:
                                    retval = retval.replaceAll("\\s+", "");
                                    String[] IdString = retval.split("=");

                                    int ID = Integer.parseInt(IdString[1]);

                                    if(!retval.contains("RESULTQUERYFORID")){
                                        LogsTime[LogsIterator++] = new LogsIdResultTime(StartTime, ID);
                                    } else {
                                        for(int i = 0; i < LogsIterator; i++){
                                            if(LogsTime[i].GetID() == ID){
                                                LogsTime[i].AddFinishTime(StartTime);
                                                break;
                                            }
                                        }
                                    }
                                    break;
                            }
                        }

                        LogString = reader.readLine();
                    }

                    Output_write(writer, "Anomaly performance time :");
                    if(AnomalyTime == 0) CriterrionGrabbs(LogsTime, LogsIterator, writer);
                    else for(int i = 0; i < LogsIterator; i++){
                            long a;
                            if((a = LogsTime[i].GetPerfomanceTime()) >= AnomalyTime)
                                Output_write(writer, "\tID : " + LogsTime[i].GetID() + " Performance time -> " + a);
                    }

                    Output_write(writer, "\n\nAll performance time :");
                    for(int i = 0; i < LogsIterator; i++) {
                        long a = LogsTime[i].GetPerfomanceTime();
                        Output_write(writer, "\tID : " + LogsTime[i].GetID() + " Performance time -> " + a);
                    }

                } catch (FileNotFoundException e) {
                    System.out.println("Error reader_non_file: " + e);
                }

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    static boolean Exists(File file) {                                          //Проверка существования файла
        if (file.exists())
            return (true);
        else
            System.out.println("##Error: file not found. Please try again.##");
        return (false);
    }

    static boolean isNumeric(String string) {                                   //Проверка на число
        try {
            Integer.parseInt(string);                                           //Если удалось перевести строку в число то вернется true
            return (true);
        } catch (NumberFormatException e) {                                     //Если при переводе произошло исключение, то вернется false
            System.out.println(e);
        }
        return (false);
    }

    static void Output_write(BufferedWriter writer, String request) {
        try {

            writer.write(request);
            writer.newLine();
            writer.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void CriterrionGrabbs(LogsIdResultTime[] array, int size, BufferedWriter writer){            //критерий граббса
        double Deviation = 0;

        for(int i = 0; i < size; i++)
            Deviation += array[i].GetPerfomanceTime();

        double MiddleNumber = Deviation/size;

        Deviation = 0;
        for(int i = 0; i < size; i++)
            Deviation += Math.pow(array[i].GetPerfomanceTime() - MiddleNumber, 2);

        Deviation = Math.sqrt(Deviation / size);

        for(int i = 0; i < size; i++) {
            long time = array[i].GetPerfomanceTime();

            if (1.445 < (time - MiddleNumber) / Deviation)
                Output_write(writer, "\tID : " + array[i].GetID() + " Performance time -> " + time);
        }
    }

    static class LogsIdResultTime{
        private long StartTime, FinishTime = 0;
        private int ID;

        LogsIdResultTime(long Start, int id){
            StartTime = Start;
            ID = id;
        }

        public void AddFinishTime(long Finish){
            FinishTime = Finish;
        }

        public long GetPerfomanceTime(){
            return FinishTime - StartTime;
        }

        public int GetID(){
            return ID;
        }
    }
}
