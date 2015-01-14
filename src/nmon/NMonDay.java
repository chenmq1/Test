package nmon;

import java.util.*;
import java.io.*;
public class NMonDay{
public double []result;
public int timeTick=0;
public double []sum;// sumHour
public double []sumDay;//sumDay
public int []countDay;
public double []maxDay;
public double []minDay;
public double []peak;
public double []min;
public int []hour;
public int []minHour;

int indexes [];
public String [][] read(String fileName,String spliter)throws Exception{
LineNumberReader lr=new LineNumberReader(new FileReader(fileName));
Vector vec=new Vector();
String line="";
while(true){
	line=lr.readLine();
	if(line==null)
		break;
	String [] values=line.split(spliter);
	for (int i=0;i<values.length;i++)
		values[i]=values[i].trim();
	vec.add(values);
}
lr.close();
String [][]result=new String[vec.size()][];
for (int i=0;i<result.length;i++)
	result[i]=(String[])(vec.elementAt(i));
return result;
}

public void start(int [] pIndexes){
indexes=pIndexes;
result=new double[indexes.length];
sum=new double[indexes.length];
peak=new double[indexes.length];
hour=new int[indexes.length];
minHour=new int[indexes.length];
min=new double[indexes.length];
sumDay=new double[indexes.length];
countDay=new int[indexes.length];
maxDay=new double[indexes.length];
minDay=new double[indexes.length];
for (int i=0;i<min.length;i++){
	min[i]=1.7976931348623157E308;
	minDay[i]=1.7976931348623157E308;
	}
}
public String [][] read(String fileName)throws Exception{
return read(fileName,",");
}

public void processLine(String [] line){
for (int i=0;i<indexes.length;i++){
	double value=Double.parseDouble(line[indexes[i]]);
	sum[i]+=value;
	sumDay[i]+=value;
	countDay[i]++;
	if (value>maxDay[i])
		maxDay[i]=value;
	if (value<minDay[i])
		minDay[i]=value;
}
if (Integer.parseInt(line[1].substring(1,line[1].length()))%12==0){
	for (int i=0;i<indexes.length;i++){
		double temp=sum[i]/12;
		if (temp>peak[i]){
			peak[i]=temp;
			hour[i]=Integer.parseInt(line[1].substring(1,line[1].length()))/12;
		}
		if (temp<min[i]){
			min[i]=temp;
			minHour[i]=Integer.parseInt(line[1].substring(1,line[1].length()))/12;
		}
		sum[i]=0;
	}	
}
}
/*public void processLine(String [] line){  // false method
	for (int i=0;i<indexes.length;i++){
		double temp=Double.parseDouble(line[indexes[i]]);
		if (temp>peak[i]){
			peak[i]=temp;
			hour[i]=Integer.parseInt(line[1].substring(1,line[1].length()))/12;
		}
		if (temp<min[i]){
			min[i]=temp;
			minHour[i]=Integer.parseInt(line[1].substring(1,line[1].length()))/12;
		}
	}	
}*/
public int [] getIndexes(String [] title){
	Vector vec=new Vector();
	for (int i=0;i<title.length ; i++ )	{
		//if ((title[i].indexOf("fcs")!=-1)||(title[i].indexOf("fscsi")!=-1)||(title[i].indexOf("scsi")!=-1)){
		//if (1==1)
		//if ((title[i].indexOf("Idle%")!=-1)||(title[i].indexOf("PhysicalCPU")!=-1)||(title[i].indexOf("entitled%")!=-1)||(title[i].indexOf("pgout")!=-1))
		//if ((title[i].equals("Idle%"))||(title[i].equals("PhysicalCPU"))||(title[i].equals("entitled"))||(title[i].equals("pgsout"))||(title[i].equals("PoolIdle")))
		//if (title[i].indexOf("Real Free %")!=-1)
		if ((title[i].equals("Idle%"))||(title[i].equals("PhysicalCPU"))||(title[i].equals("entitled"))||(title[i].equals("pgsout"))||(title[i].equals("PoolIdle"))||(title[i].equals("virtualCPUs")))
			vec.add(new Integer(i+2));
	}
	int result []=new int[vec.size()];
	for (int i=0;i<result.length ;i++ )	{
		result[i]=((Integer)(vec.elementAt(i))).intValue();
	}
	return result;
}
public void getData(NMonServer server,String filename,String date)throws Exception{
	
	String rows[][]=DbUtil.query("select rowcolumns,rowId from row where rowId in (1,12,68,1912)");
	//String rows[][]=DbUtil.query("select rowcolumns,rowId from row where rowId in (select rowId from row where rowName='MEM')");
for (int j=0;j<rows.length;j++){
		
	
	String [] title=rows[j][0].split(",");
	start(getIndexes(title));
	String sql="select datastring from nmondata where rowid="+rows[j][1]+" and reportid="+filename;
	String [][] data=DbUtil.query(sql);
	if (data.length<12)
		continue;
	for (int i=0;i<data.length ;i++ ){
		processLine(data[i][0].split(","));
	}
	
	Column result[]=new Column [indexes.length];
	for (int i=0;i<indexes.length ;i++ ){
		result[i]=new Column();
		result[i].columnValue=peak[i];
		result[i].date=date;
		result[i].hour=hour[i];
		result[i].minHour=minHour[i];
		result[i].columnMinValue=min[i];
		result[i].sum=sumDay[i];
		result[i].count=countDay[i];
		result[i].max=maxDay[i];
		result[i].min=minDay[i];
		server.addDayValue(title[indexes[i]-2],result[i]);	
	}
}
}
public static void main(String args[])throws Exception{

}
}