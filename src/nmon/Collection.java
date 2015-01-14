/**
 * 
 */

package nmon;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.*;

import java.text.*;
import java.util.*;
import java.awt.Color;
import java.io.*;
import java.sql.Connection;
/**
 * @author minqi
 *
 */
public class Collection {
public TimeSeriesCollection m_collection=null;
public Hashtable m_data;
public Hashtable m_statisticData;
public String date="";
public RegularTimePeriod [] m_tp;
public Connection m_con;
public Collection(Connection con){
	m_tp=new RegularTimePeriod [288];
	m_collection=new TimeSeriesCollection();
	m_data=new Hashtable();
	m_statisticData=new Hashtable();
	m_con=con;
}
public void setTimeTicks(String pDate)throws Exception{
	String date="20"+pDate;
	SimpleDateFormat smf=new SimpleDateFormat("yyyyMMdd");
	Date startTime=smf.parse(date);
	Calendar c=Calendar.getInstance();
	c.setTime(startTime);
	for (int i=0;i<m_tp.length;i++){
	/*	c.add(Calendar.MINUTE, 5);
		Date endTime=c.getTime();
		m_tp[i]=new SimpleTimePeriod(startTime,endTime);
		startTime=endTime;*/
		m_tp[i]=new Minute(c.getTime());
		c.add(Calendar.MINUTE, 5);
	}
}
public void add(String rowId,String tick,String data)throws Exception{
	
	TimeSeriesCollection ts=(TimeSeriesCollection)(m_data.get(rowId));
	Statistic []st=(Statistic[])(m_statisticData.get(rowId));
	if (ts==null){
		String result[][]=DbUtil.query("select rowColumns from row where rowId="+rowId,m_con);
		ts=new TimeSeriesCollection();
		String names[]=result[0][0].split(",");
		st=new Statistic[names.length];
		for (int i=0;i<names.length;i++){
				ts.addSeries(new TimeSeries(names[i]));
				st[i]=new Statistic();
		}
		m_data.put(rowId, ts);
		m_statisticData.put(rowId, st);
	}
	int iTick=Integer.parseInt(tick);
	if (iTick>=m_tp.length){
		System.out.println("error Tick:"+tick);
		return;
	}
	String [] values=data.split(",");
	for (int i=2;i<values.length;i++){
		double dValue=0;
		try {
			dValue=Double.parseDouble(values[i]);
		}catch (Exception e){}
		if ((i-2)<ts.getSeriesCount()){
			ts.getSeries(i-2).add(m_tp[iTick-1], dValue);
			st[i-2].newValue(dValue);
		}
		
	}	
}
public String[] drawCharts(String path)throws Exception{
	Enumeration keys=m_data.keys();
	String [] result=new String[m_data.size()];
	int index=0;
	while (keys.hasMoreElements()){
		String rowId=(String)(keys.nextElement());
		String rowName=DbUtil.query("select rowName from row where rowId="+rowId,m_con)[0][0];
		TimeSeriesCollection tsc=(TimeSeriesCollection)m_data.get(rowId);
		//Enumeration colKeys=server.columns.keys();
		File f = new File(path+rowName+".png");
		result[index++]=rowName+".png";
		JFreeChart jfc=ChartFactory.createTimeSeriesChart(rowName,null,null,tsc,true,true,false);
		jfc.setBackgroundPaint(Color.white);
		XYPlot plot = jfc.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);

		ChartUtilities.saveChartAsPNG(f,jfc,700,300);
	}
	return result;

}
public String[][] drawCharts(String path,String[] requiredCharts,String [][] requiredLines)throws Exception{
	String [][] result=new String[requiredCharts.length][];
	for (int i=0;i<result.length;i++){
		String rowName=requiredCharts[i];
		String [][]rowIds=DbUtil.query("select rowId from row where rowName='"+rowName+"'",m_con);
		for (int j=0;j<rowIds.length;j++){
			TimeSeriesCollection tsc=(TimeSeriesCollection)m_data.get(rowIds[j][0]);
			if (tsc==null)
				continue;
			Statistic [] sts=(Statistic[])(m_statisticData.get(rowIds[j][0]));
			File f = new File(path+rowName+".png");
			JFreeChart jfc=ChartFactory.createTimeSeriesChart(rowName,null,null,tsc,true,true,false);
			jfc.setBackgroundPaint(Color.white);
			XYPlot plot = jfc.getXYPlot();
	        plot.setBackgroundPaint(Color.white);
	        plot.setDomainGridlinePaint(Color.lightGray);
	        plot.setRangeGridlinePaint(Color.lightGray);
			if ((requiredLines[i]!=null)&&(requiredLines.length>0)){
				result[i]=new String[1+requiredLines[i].length];
				int index=0;
				int count=tsc.getSeriesCount();
				for (int k=0;k<count;k++){
					boolean found=false;
					String seriesName=(String)(tsc.getSeries(index).getKey());
					for (int l=0;l<requiredLines[i].length;l++){
						if (requiredLines[i][l].equals(seriesName)){
							found =true;
							break;
						}
					}
					if (found==false){
						tsc.removeSeries(index);
					}else{
						result[i][1+index]=seriesName+"--"+sts[k].output();
						index++;
					}
				}
				String temp[]=new String [1+index];
				for (int k=0;k<temp.length;k++)
					temp[k]=result[i][k];
				result[i]=temp;
			}else{
				int count=tsc.getSeriesCount();
				result[i]=new String[1+count];
				for (int k=0;k<count;k++){
					String seriesName=(String)(tsc.getSeries(k).getKey());
					result[i][1+k]=seriesName+"--"+sts[k].output();
				}
			}
	        
	        ChartUtilities.saveChartAsPNG(f,jfc,700,300);
			
			result[i][0]=rowName+".png";
			break;
		}
	}
		
	return result;
}
}
class Statistic{
	public double sum=0;
	public int count=0;
	public double max=4.9E-324;
	public double min=1.7976931348623157E308;
	public void newValue(double newV){
		sum+=newV;
		count++;
		if (max<newV)
			max=newV;
		if (min>newV)
			min=newV;
	}
	public String output(){
		String result="Max:\t"+new   DecimalFormat("###,###,###.##").format(max)+"\taverage:\t"+new   DecimalFormat("###,###,###.##").format(sum/count)+"\tMin:\t"+new   DecimalFormat("###,###,###.##").format(min);
		return result;
	}
}