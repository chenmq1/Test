/**
 * 
 */
package nmon;
import java.io.*;
/**
 * @author minqi
 *Jan 7, 2013
 */
public class TrendReport {
public LineChartReport mLineChartReport;
public TrendReport(){
	mLineChartReport=new LineChartReport();
}
	/**
	 * @param args
	 */
	
	public String [] hostWeek(String hostId,String earliestDate)throws Exception{
		String sql="select name from peakHour where hostId="+hostId+" and date>'"+earliestDate+
				"' group by name";
		String names[][]=DbUtil.query(sql);
		String []result=new String [names.length];
		for (int i=0;i<names.length;i++){
			result[i]= hostId+"week"+earliestDate+i+".png";
			//System.out.println(result[i]);
			mLineChartReport.createWeekReport(hostId, names[i][0], earliestDate,result[i]);
			}
		return result;
	}
	
	public String [] hostMonth(String hostId,String month)throws Exception{
		String sql="select name from peakHour where hostId="+hostId+" and date like'"+month+
				"%' group by name";
		String names[][]=DbUtil.query(sql);
		String []result=new String [names.length];
		for (int i=0;i<names.length;i++){
			result[i]= hostId+"month"+month+i+".png";
			mLineChartReport.createMonthReport(hostId, names[i][0], month,result[i]);
			}
		return result;
	}
	public void week(String earliestDate,String filename)throws Exception{
		
		PrintWriter pw=new PrintWriter(new FileWriter(filename));
		pw.print("<html><body>");
		String sql="select host.hostId,hostname,app from peakHour,host where host.hostId=peakHour.hostId and date>'"+earliestDate+"' group by host.hostId,hostname,app order by app";
		String hosts[][]=DbUtil.query(sql);
		String app="none";
		for (int i=0;i<hosts.length;i++){
			if (!app.equals(hosts[i][2])){
				app=hosts[i][2];
				pw.println("<p><b>Application:"+app+"</b></p>");}
			pw.println("<p>"+hosts[i][1]+"</p>");
			String []pics=hostWeek(hosts[i][0],earliestDate);
			for (int j=0;j<pics.length;j++)
				pw.println("<img src="+pics[j]+">");
		}
		pw.println("</body></html>");
		pw.close();
	}
	public static void main(String[] args)throws Exception {
		TrendReport tr=new TrendReport();
		tr.week("121216", "week.html");

	}

}
