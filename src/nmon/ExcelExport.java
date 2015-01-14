/**
 * 
 */
package nmon;
import java.io.*;
import java.sql.*;
import jxl.*;
import jxl.write.*;
/**
 * @author minqi
 *
 */
public class ExcelExport {
public static final String OUTPUT_PATH="c:\\Program Files (x86)\\Apache Software Foundation\\Tomcat 5.5\\webapps\\ROOT\\nmonExcel\\";
public void exportFile(String reportId,String filename)throws Exception{
	String sql="select rowId,tickIndex,dataString from nmonData where  reportId="+reportId+" order by rowId,tickIndex";
	String [][]result=DbUtil.query(sql);
	System.out.println("data returned");
	WritableWorkbook wb=Workbook.createWorkbook(new File(OUTPUT_PATH+filename));
	boolean newRow=true;
	String []rowCols=null;
	int index=0;
	int sheetIndex=0;
	int rowIndex=0;
	WritableSheet ws=null;
	String rowId="impossibleRowId";
	while (index<result.length){
		if (!rowId.equals(result[index][0])){
			String rowInfo[][]=DbUtil.query("select rowName,rowColumns from row where rowId="+result[index][0]);
			ws=wb.createSheet(rowInfo[0][0], sheetIndex++);
			String title[]=rowInfo[0][1].split(",");
			for (int i=0;i<title.length;i++){
				Label l=new Label(i+1,0,title[i]);
				ws.addCell(l);
			}
			rowIndex=1;
			rowId=result[index][0];
		}
		jxl.write.Number t=new jxl.write.Number(0,rowIndex,Double.parseDouble(result[index][1]));
		ws.addCell(t);
		String numbers[]=result[index][2].split(",");
		for (int i=2;i<numbers.length;i++){
			if (numbers[i].length()==0){
				jxl.write.Label n=new jxl.write.Label(i-1,rowIndex,"");
				ws.addCell(n);
			}else{
				jxl.write.Number n=new jxl.write.Number(i-1,rowIndex,Double.parseDouble(numbers[i]));
				ws.addCell(n);
			}
			//System.out.print(numbers[i]+" ");
		}
		//System.out.println(numbers.length);
		rowIndex++;
		index++;
	}
	wb.write();
	wb.close();
}
	/**
	 * @param args
	 */
	public static void main(String[] args)throws Exception {
		ExcelExport ee=new ExcelExport();
		ee.exportFile("1", "d:\\temp.xls");

	}

}
