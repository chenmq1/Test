/**
 * 
 */
package nmon;
import java.io.*;
import java.sql.*;
import java.util.*;
/**
 * @author chenmq
 *
 */
public class Import {

public static String [] relatedColumns=new String []{"PAGE","PROC","MEM","MEMUSE","CPU[0-9]+","CPU_ALL","LPAR","IOADAPT"};

private Connection con;
private PreparedStatement queryHostname;
private PreparedStatement insertHostname;
private PreparedStatement insertColume;
/*private PreparedStatement 
private PreparedStatement 
private PreparedStatement */
public static final String inputPath="/nmonFiles/";
public static final String outputPath="/toLoad/";
private int maxTick;
public Import(){
}
public void init()throws Exception{
	con=DbUtil.getConnection();
//	queryHostname=con.prepareStatement("select hostId from host where hostname=?");
//	insertHostname=con.prepareStatement("select hostId from FINAL TABLE(insert into host(hostname)values(?))");
	insertColume=con.prepareStatement("insert into nmonData(reportId,");
}

public void processHeader(LineNumberReader lr)throws Exception{
	//return new FileHeader();
}
	
public void processLine(String hostId,String reportId,String line,PrintWriter pw,Hashtable rowIds,int latestTick)throws Exception{
	//System.out.println("test");
	String cols[]=line.split(",");
	for (int i=0;i<relatedColumns.length;i++){
		if (cols[0].matches(relatedColumns[i])){
			if (!cols[1].matches("T[0-9]{4}")){
				String columns=cols[2];
				for (int j=3;j<cols.length;j++)
					columns+=","+cols[j];
				String result [][]=DbUtil.query ("select rowId from row where rowName='"+cols[0]+"' and rowColumns='"+columns+"'");
				// need performance improvement of row id selection
				
				if (result.length==0){
					
					result=DbUtil.query ("select rowId from FINAL TABLE(insert into row(rowName,rowColumns)values('"+cols[0]+"','"+columns+"'))");
				}
				String rowId=result[0][0];
				rowIds.put(cols[0], rowId);
			}else{
				String rowId=(String)rowIds.get(cols[0]);
				if (rowId==null)
					continue;
				int tickIndex=Integer.parseInt(cols[1].substring(1,cols[1].length()));
				if (tickIndex>latestTick)
					//System.out.println(reportId+"\t"+rowId+"\t"+tickIndex+"\t"+line);
					pw.println(reportId+"\t"+rowId+"\t"+tickIndex+"\t"+line);
				maxTick=tickIndex;
				
			}
		}
	}	
}
public void importFile(String fileName)throws Exception{
	String nameInfo[]=fileName.split("_");
	if (nameInfo.length<3){
		return;
	}
	String hostname=nameInfo[0];
	String date=nameInfo[1];
	
	//String time=nameInfo[2].split(".")[0];
	// to add================== check if time is not 0000, then skip the file.
	if (nameInfo[2].indexOf("0000")!=0)
		return;
	//queryHostname.setString(1,host);
	//System.out.println("processing host:"+hostname);
	String result [][]=DbUtil.query ("select hostId from host where hostname='"+hostname+"'");
	if (result.length==0){
		result=DbUtil.query ("select hostId from FINAL TABLE(insert into host(hostname)values('"+hostname+"'))");
	}
	String hostId=result[0][0];
	
	result=DbUtil.query ("select reportId,latestTick from report where hostId="+hostId+" and date='"+date+"'");
	if (result.length==0){
		result=DbUtil.query ("select reportId,latestTick from FINAL TABLE(insert into report(hostId,date)values("+hostId+",'"+date+"'))");
	}
	String reportId=result[0][0];
	int latestTick=Integer.parseInt(result[0][1]);
	new Integer(result[0][1]);
	LineNumberReader lr=new LineNumberReader(new FileReader(inputPath+"/"+fileName));
	PrintWriter pw=new PrintWriter(new FileWriter(outputPath+fileName+".toLoad"));
	processHeader(lr);
	String line=lr.readLine();
	Hashtable rowIds=new Hashtable();
	maxTick=latestTick;
	while (line!=null){
		processLine(hostId,reportId,line,pw,rowIds,latestTick);
		line=lr.readLine();
	}
	if (maxTick>latestTick)
		DbUtil.excute("update report set latestTick="+maxTick+" where reportId="+reportId);
	pw.close();
	lr.close();
}
public static void main(String[] args)throws Exception{
	Import i=new Import();
	i.init();
	i.importFile(args[0]);
}
}
