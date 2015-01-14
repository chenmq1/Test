/**
 * 
 */
package nmon;
import java.awt.Color;
import java.io.File;
import java.util.Calendar;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.util.SortOrder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;

/**
 * @author minqi
 *Jan 6, 2013
 */
public class LineChartReport {
	private static String [] show={"None","Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	private int decreaseMonth(int month){
		int nLastMonth=month-1;
		if ((nLastMonth%100)==99)
			nLastMonth-=87;
		return nLastMonth;
	}
	/*private Comparable dayAsColumnKey(String date){
			Calendar cDate=getDate(date);
			return new DayKey(cDate.get(Calendar.DAY_OF_WEEK));			
		}*/
	private Calendar getDate(String date){
		Calendar c=Calendar.getInstance();
		c.clear();
		c.set(Integer.parseInt(date.substring(0,2))+2000, Integer.parseInt(date.substring(2,4))-1,Integer.parseInt(date.substring(4,6)));
		return c;
	}
	public boolean  createMonthReport(String hostId,String name,String targetMonth,String filename)throws Exception{
		int nTargetMonth=Integer.parseInt(targetMonth);
		int nLastMonth=decreaseMonth(nTargetMonth);
		int nLastLastMonth=decreaseMonth(nLastMonth);
		String sql="select substr(date,1,4),substr(date,5,2),bottomhourutil from peakhour where hostId="+hostId+
				" and date>'"+nLastLastMonth+"00' and name='"+name+"'";
		String data[][]=DbUtil.query(sql);
		if (data.length==0)
			return false;
		DefaultCategoryDataset dcd=new DefaultCategoryDataset();
		for (int i=0;i<data.length;i++)
			for (int j=2;j<data[i].length;j++)
				dcd.addValue(Double.parseDouble(data[i][j]), data[i][0], data[i][1]);
		JFreeChart jfc=ChartFactory.createLineChart(name,null,null,dcd,PlotOrientation.VERTICAL ,true,true,false);
		jfc.setBackgroundPaint(Color.white);
		CategoryPlot plot = jfc.getCategoryPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);
        File f = new File(filename);
		ChartUtilities.saveChartAsPNG(f,jfc,700,300);
		return true;
	}
	public boolean  createWeekReport(String hostId,String name,String earlestDate,String filename)throws Exception{
		String sql="select date,bottomhourutil from peakhour where hostId="+hostId+
				" and date>='"+earlestDate+"' and name='"+name+"'";
		String data[][]=DbUtil.query(sql);
		if (data.length==0)
			return false;
		DefaultCategoryDataset dcd=new DefaultCategoryDataset();
		for (int i=0;i<data.length;i++)
			for (int j=1;j<data[i].length;j++)
				dcd.setValue(Double.parseDouble(data[i][j]),  "week-"+getDate(data[i][0]).get(Calendar.WEEK_OF_YEAR),show[getDate(data[i][0]).get(Calendar.DAY_OF_WEEK)]);
		
		
		
		JFreeChart jfc=ChartFactory.createLineChart(name,null,null,dcd,PlotOrientation.VERTICAL ,true,true,false);
		jfc.setBackgroundPaint(Color.white);
		CategoryPlot plot = jfc.getCategoryPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.lightGray);
        plot.setRangeGridlinePaint(Color.lightGray);
        plot.setColumnRenderingOrder(SortOrder.ASCENDING);
        File f = new File(filename);
		ChartUtilities.saveChartAsPNG(f,jfc,700,300);
		return true;

	}
	//private 
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		LineChartReport lcr=new LineChartReport();
		lcr.createMonthReport("42", "Idle%", "1212", "d:\\month.png");
		lcr.createWeekReport("42", "Idle%", "121216", "d:\\week.png");
	}

}
/*class DayKey implements Comparable<DayKey>{
	private static String [] show={"None","Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	private int m_dayIndex;
	public int compareTo(DayKey other){
		int result=m_dayIndex-other.m_dayIndex;
	        return result;
	    }
	public boolean equals(Object o){
		return (0==compareTo((DayKey)o));
	}
	public String toString(){
		return show[m_dayIndex];
	}
	public DayKey(int index){
		m_dayIndex=index;
	}
}
*/