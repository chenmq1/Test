/**
 * 
 */
package nmon;

/**
 * @author minqi
 *
 */
import java.sql.*;
import java.io.*;
import java.util.*;
import java.text.*;

public class Util2{

public int renderSql(String sql,Writer pOut)throws Exception{
PrintWriter out=new PrintWriter(pOut);
String [][] values=DbUtil.query(sql);
int i=0;
for (;i<values.length;i++){
	if (i%2!=0)
		out.println("<tr style=\"background-color:cccccc\">");
	else
		out.println("<tr style=\"background-color:white\">");
	for (int j=0;j<values[i].length;j++){
		if ((values[i][j]==null)||(values[i][j].trim().length()==0))
			out.println("<td>&nbsp;</td>");
		else
			out.println("<td>"+values[i][j]+"</td>");
		}
	out.println("</tr>");
}
return i;
}




}


