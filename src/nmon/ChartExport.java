/**
 * 
 */
package nmon;

import java.io.File;
import java.sql.Connection;
import java.util.*;
import java.text.*;

/**
 * @author minqi
 *
 */
public class ChartExport {
	
	//public static final String OUTPUT_PATH="c:\\Program Files (x86)\\Apache Software Foundation\\Tomcat 5.5\\webapps\\ROOT\\nmonExcel\\";
	public Connection m_con;
	

	public void collect(String reportId,Collection col,String timeTickSql)throws Exception{
		if (reportId==null)
			return;
		String sql="select date from report where reportId="+reportId;
		String result[][]=DbUtil.query(sql,m_con);
		String date=result[0][0];
		col.setTimeTicks(date);
		sql="select rowId,tickIndex,dataString from nmonData where  reportId="+reportId;
		if (timeTickSql.length()!=0)
			sql+=" and "+timeTickSql;
		result=DbUtil.query(sql,m_con);
		for (int i=0;i<result.length;i++){
			col.add(result[i][0],result[i][1],result[i][2]);
		}
	}
	public Collection collectPeriod(Date start,Date end,String hostId) throws Exception{
		Collection col=new Collection(m_con);
		Calendar cStart=Calendar.getInstance();
		cStart.setTime(start);
		Calendar cEnd=Calendar.getInstance();
		cEnd.setTime(end);
		if (cEnd.before(cStart))
			return null;
		String startDate=getDate(start);
		String endDate=getDate(end);
		if (startDate.equals(endDate)){
			collect(getReportId(startDate,hostId),col," tickIndex>"+getTick(cStart)+" and tickIndex<"+getTick(cEnd));
			return col;
		}
		collect(getReportId(startDate,hostId),col," tickIndex>"+getTick(cStart));
		while (true){
			cStart.add(Calendar.DAY_OF_MONTH, 1);
			if ((cStart.get(Calendar.DAY_OF_MONTH)==cEnd.get(Calendar.DAY_OF_MONTH))&&
					(cStart.get(Calendar.MONTH)==cEnd.get(Calendar.MONTH)&&
					(cStart.get(Calendar.YEAR)==cEnd.get(Calendar.YEAR))))
				break;
			collect(getReportId(getDate(cStart.getTime()),hostId),col,"");
		}
		collect(getReportId(endDate,hostId),col," tickIndex<"+getTick(cEnd));
		return col;			
	}
	
	private String getReportId(String date,String hostId)throws Exception{
		String sql="select reportId from report where hostId="+hostId+" and date='"+date+"'";
		String result[][]=DbUtil.query(sql,m_con);
		if (result.length==0)
			return null;
		else
			return result[0][0];
	}
	private int getTick(Calendar c){
		int hour=c.get(Calendar.HOUR_OF_DAY);
		int min=c.get(Calendar.MINUTE);
		min+=hour*60;
		return min/5;
	}
	private String getDate(Date d){
		SimpleDateFormat sdf=new SimpleDateFormat("yyMMdd");
		return sdf.format(d);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)throws Exception {
		SimpleDateFormat sdf=new SimpleDateFormat("yyMMddhhmm");
		ChartExport ce=new ChartExport();
		ce.m_con=DbUtil.getConnection();
		Collection c=ce.collectPeriod(sdf.parse("1204022011"), sdf.parse("1204042011"), "2");
		c.drawCharts("c:\\test\\");
		ce.m_con.close();

	}

}
