/**
 * 
 */
package nmon;

/**
 * @author minqi
 *
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

public class DbUtil {
	//static String URL="jdbc:db2://10.96.25.174:50010/nmon";
	static String URL="jdbc:db2://10.176.66.156:50010/nmon";
	static String username="db2nmon";

	static String password="C00lingdown";

	static boolean initialized=false;



	
	public static Connection getConnection()throws Exception{

		if (!initialized)

			initialize();

		return DriverManager.getConnection(URL,username,password);

		}





	public static void initialize()throws Exception{
		//Class.forName("COM.ibm.db2.jdbc.app.DB2Driver").newInstance();
		Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();
		initialized=true;

		}

	public static void main(String [] args)throws Exception{

	//try{

		Class.forName("COM.ibm.db2.jdbc.net.DB2Driver").newInstance();

		Connection con=DriverManager.getConnection(URL,username,password);

	//}catch (Exception e){

//		System.out.println(e.toString());

	//}

	}

	public static boolean excute(String sql)throws Exception{

		System.out.println(sql);

		Connection con = getConnection();

		Statement stmt=con.createStatement();

		try{	

	 	stmt.execute(sql);

		}catch (Exception e){ System.out.print(e.toString());

					return false;}

		stmt.close();

		con.close();

		return true;	

		}

	public static String [][] query(String sql) throws Exception{

		System.out.println(sql);

		Connection con = getConnection();

		Statement stmt=con.createStatement();

		ResultSet rs=stmt.executeQuery(sql);

		int colNum=rs.getMetaData().getColumnCount();

		Vector vec=new Vector();

		while(rs.next()){

			String [] row=new String[colNum];

			for (int i=0;i<colNum;i++)

				row[i]=rs.getString(i+1);

			vec.add(row);

		}

		rs.close();

		stmt.close();

		con.close();

		String [][]result=new String[vec.size()][];

		for (int i=0;i<result.length;i++)

			result[i]=(String[])(vec.elementAt(i));

		//Loger.log("result.length:"+result.length);

		return result;

	}
	public static String [][] query(String sql,Connection con) throws Exception{

		System.out.println(sql);

		
		Statement stmt=con.createStatement();

		ResultSet rs=stmt.executeQuery(sql);

		int colNum=rs.getMetaData().getColumnCount();

		Vector vec=new Vector();

		while(rs.next()){

			String [] row=new String[colNum];

			for (int i=0;i<colNum;i++)

				row[i]=rs.getString(i+1);

			vec.add(row);

		}

		rs.close();

		stmt.close();

		String [][]result=new String[vec.size()][];

		for (int i=0;i<result.length;i++)

			result[i]=(String[])(vec.elementAt(i));

		//Loger.log("result.length:"+result.length);

		return result;

	}
}

