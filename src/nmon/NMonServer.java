package nmon;
import java.io.*;
import java.util.*;

import jxl.*;
import jxl.write.*;
public class NMonServer{
public String serverName;
public String hostId;
public Hashtable columns;

public NMonServer(String pServerName){
	serverName=pServerName;
	columns=new Hashtable();
}
public NMonServer(String pServerName,String pHostId){
	this(pServerName);
	hostId=pHostId;
}
public void addDayValue(String columnName,Column col){
	//System.out.println("added");
	Vector vec=(Vector)(columns.get(columnName));
	if (vec==null){
		vec=new Vector();
		columns.put(columnName,vec);
	}
	vec.add(col);
}

public static void main(String args[])throws Exception{
	final String[] titleSub=new String []{"max","avg","min","maxPeakH","avgPeakH","maxPeakHV","avgPeakHV"};
	
	Hashtable servers=new Hashtable();
	//String rootDir="D:\\upgrade\\nmonData\\nmondata\\";
	String sql="select hostname,date,reportId from host,report where report.hostid=host.hostid and date>'121031' and date<'121201' ";//and host.hostname like 'PEKII%' ";//and (host.hostname='PEKAX028' or host.hostname='PEKAX029')";
			//date like '1205%'";// and host.hostname='PEKAX028'";
	String outputFile="D:\\nmonResult.txt";
	WritableWorkbook wb=Workbook.createWorkbook(new File("d:\\nmonResult.xls"));
	WritableSheet ws=wb.createSheet("Sheet1", 0);
	
	Vector titles=new Vector(); 
	
	String files[][]=DbUtil.query(sql);
	for (int i=0;i<files.length;i++){
		try{
		String []cutFile=files[i];
		NMonServer server=(NMonServer)(servers.get(cutFile[0]));
		if (server==null){
			server=new NMonServer(cutFile[0]);
			servers.put(cutFile[0],server);
		}
		NMonDay nmd=new NMonDay();
		nmd.getData(server,cutFile[2],cutFile[1]);
		}catch (Exception e){System.out.println("error while processing:"+files[i][2]+" ,"+e.toString());}
	}
	Enumeration keys=servers.keys();
	//PrintStream ps =new PrintStream(outputFile);
	int lineIndex=2;
	while (keys.hasMoreElements()){
		String serverName=(String)(keys.nextElement());
		//System.out.println(serverName);
		
		NMonServer server=(NMonServer)servers.get(serverName);
		//Enumeration colKeys=server.columns.keys();
		TreeMap tm=new TreeMap(server.columns);
		Column peaks[]=new Column[server.columns.size()];
		Column mins[]=new Column[server.columns.size()];
		String []title=new String[server.columns.size()];
		double []peaksSum=new double[server.columns.size()];
		double []minsSum=new double[server.columns.size()];
		double []allSum=new double[server.columns.size()];
		double []max=new double[server.columns.size()];
		double []min=new double[server.columns.size()];
		double []allCount=new double[server.columns.size()];
		System.out.println(server.columns.size());
		/*for (int i=0;i<peaks.length;i++){
			peaks[i]=new Column();
			peaks[i].columnValue=0;}*/
		int colIndex=0;
		String colKey=null;
		boolean hasElements=true;
		try {
		colKey=(String)(tm.firstKey());
		}catch (Exception e){hasElements=false;}
		if (hasElements)
		do {
			//System.out.print(colKey+":");
			
			title[colIndex]=colKey;
			Vector vec=(Vector)(server.columns.get(colKey));
			min[colIndex]=1.7976931348623157E308;
			for (int i=0;i<vec.size() ;i++ ){
				Column col=(Column)(vec.elementAt(i));
				if ((peaks[colIndex]==null)||(col.columnValue>peaks[colIndex].columnValue))
					peaks[colIndex]=col;
				peaksSum[colIndex]+=col.columnValue;
				if ((mins[colIndex]==null)||(col.columnMinValue<mins[colIndex].columnMinValue))
					mins[colIndex]=col;
				//if (col.columnMinValue>10000)
					//System.out.println(col.date+","+col.hour);
				minsSum[colIndex]+=col.columnMinValue;
				allSum[colIndex]+=col.sum;
				allCount[colIndex]+=col.count;
				if (max[colIndex]<col.max)
					max[colIndex]=col.max;
				if (min[colIndex]>col.min)
					min[colIndex]=col.min;
			}
			peaksSum[colIndex]/=vec.size();
			minsSum[colIndex]/=vec.size();
			allSum[colIndex]/=allCount[colIndex];
			colIndex++;
			colKey=(String)(tm.higherKey(colKey));
		}while (colKey!=null);
		Label l=new Label(0,lineIndex,serverName);
		ws.addCell(l);
		for (int i=0;i<peaks.length;i++){
			int titleIndex=-1;
			for (int j=0;j<titles.size();j++){
				if (title[i].equals(titles.elementAt(j))){
					titleIndex=j;
					break;
				}					
			}
			if (titleIndex==-1){
				titleIndex=titles.size();
				titles.add(title[i]);
			}
			int offset=1;
			l=new Label((offset++)+titleIndex*7,lineIndex,String.format("%.2f",max[i]));
			ws.addCell(l);
			l=new Label((offset++)+titleIndex*7,lineIndex,String.format("%.2f",allSum[i]));
			ws.addCell(l);
			l=new Label((offset++)+titleIndex*7,lineIndex,String.format("%.2f",min[i]));
			ws.addCell(l);
			l=new Label((offset++)+titleIndex*7,lineIndex,String.format("%.2f",peaks[i].columnValue));
			ws.addCell(l);
			l=new Label((offset++)+titleIndex*7,lineIndex,String.format("%.2f",peaksSum[i]));
			ws.addCell(l);
			l=new Label((offset++)+titleIndex*7,lineIndex,String.format("%.2f",mins[i].columnMinValue));
			ws.addCell(l);
			l=new Label((offset++)+titleIndex*7,lineIndex,String.format("%.2f",minsSum[i]));
			ws.addCell(l);
			
			/*ps.format("%.0f\t",max[i]);
			ps.format("%.0f\t",allSum[i]);
			ps.format("%.0f\t",min[i]);
			ps.format("%.0f\t",peaks[i].columnValue);
			ps.format("%.0f\t",peaksSum[i]);
			ps.format("%.0f\t",mins[i].columnMinValue);
			ps.format("%.0f\t",minsSum[i]);*/
			//ps.format("%.0f",100-mins[i].columnMinValue);
			//ps.print(","+mins[i].date+"."+mins[i].hour);
			//ps.print("\t");
			//ps.format("%.0f",100-minsSum[i]);
			}
		lineIndex++;
		//ps.println();
		//ps.println();
	}
	//ps.close();
	for (int i=0;i<titles.size();i++){
		Label l=new Label(1+i*7,0,(String)(titles.elementAt(i)));
		System.out.println((String)(titles.elementAt(i)));
		ws.addCell(l);
		for (int j=0;j<titleSub.length;j++){
			l=new Label(1+j+i*7,1,titleSub[j]);
			ws.addCell(l);
		}
	}
	wb.write();
	wb.close();
}
}