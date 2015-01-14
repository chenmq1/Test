/**
 * 
 */
package nmon;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.Vector;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import java.sql.*;
/**
 * @author minqi
 *Dec 21, 2012
 */
public class PeakHourCalculator {

	/**
	 * @param args
	 */
	public void calculate(String sqlToAppend)throws Exception{
		Connection con=DbUtil.getConnection();
		PreparedStatement ps=con.prepareStatement("merge into peakhour using (select ? as hostId,? as date,? as name,? as peakHourUtil,? as peakHour,? as bottomHourUtil,? as bottomHour,? as sum,? as max,? as min,? as cnt from sysibm.sysdummy1) tmp on (peakhour.hostId=tmp.hostId and peakhour.date=tmp.date and peakhour.name=tmp.name) " +
				"when matched then update set peakHourUtil=tmp.peakHourUtil,peakHour=tmp.peakHour,bottomHourUtil=tmp.bottomHourUtil,bottomHour=tmp.bottomHour,sum=tmp.sum,max=tmp.max,min=tmp.min,cnt=tmp.cnt when not matched then "+
				"insert (hostId,date,name,peakHourUtil,peakHour,bottomHourUtil,bottomHour,sum,max,min,cnt) values (tmp.hostId,tmp.date,tmp.name,tmp.peakHourUtil,tmp.peakHour,tmp.bottomHourUtil,tmp.bottomHour,tmp.sum,tmp.max,tmp.min,tmp.cnt)");
		Hashtable servers=new Hashtable();
		String sql="select hostname,date,reportId,host.hostId from host,report where report.hostid=host.hostid "+sqlToAppend;//and host.hostname like 'PEKII%' ";//and (host.hostname='PEKAX028' or host.hostname='PEKAX029')";
				//date like '1205%'";// and host.hostname='PEKAX028'";
		Vector titles=new Vector(); 
		
		String files[][]=DbUtil.query(sql);
		for (int i=0;i<files.length;i++){
			try{
			NMonServer server=(NMonServer)(servers.get(files[i][0]));
			if (server==null){
				server=new NMonServer(files[i][0],files[i][3]);
				servers.put(files[i][0],server);
			}
			NMonDay nmd=new NMonDay();
			nmd.getData(server,files[i][2],files[i][1]);
			}catch (Exception e){System.out.println("error while processing:"+files[i][2]+" ,"+e.toString());}
		}
		Enumeration keys=servers.keys();
		while (keys.hasMoreElements()){
			String serverName=(String)(keys.nextElement());
			
			NMonServer server=(NMonServer)servers.get(serverName);
			//Enumeration colKeys=server.columns.keys();
			TreeMap tm=new TreeMap(server.columns);//use treemap to sort, but don't need it now
			String colKey=null;
			boolean hasElements=true;
			try {
			colKey=(String)(tm.firstKey());
			}catch (Exception e){hasElements=false;}
			if (hasElements)
			do {
				//System.out.print(colKey+":");
				
				String name=colKey;
				Vector vec=(Vector)(server.columns.get(colKey));
				ps.setInt(1, Integer.parseInt(server.hostId));
				for (int i=0;i<vec.size() ;i++ ){
					System.out.println("executing "+name);
					ps.setString(3, name);
					Column col=(Column)(vec.elementAt(i));
					ps.setString(2, col.date);
					ps.setDouble(4, col.columnValue);
					ps.setInt(5,col.hour);
					ps.setDouble(6, col.columnMinValue);
					ps.setInt(7, col.minHour);
					ps.setDouble(8,col.sum);
					ps.setDouble(9,col.max);
					ps.setDouble(10,col.min);
					ps.setInt(11,col.count);
					ps.execute();		
					//ps.clearParameters();
			//		System.out.println("executed."+vec.size());
				}
				colKey=(String)(tm.higherKey(colKey));
			}while (colKey!=null);
		}
		//System.out.println("exiting...");
		ps.close();
		con.close();
	}
	
	public static void main(String[] args)throws Exception {
		PeakHourCalculator phc=new PeakHourCalculator();
		phc.calculate(" and date like '1212%'"); //and hostname='PEKAX028'");
	}
}
