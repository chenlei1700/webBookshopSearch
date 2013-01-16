package jp.co.ns_sol.sysrdc.abs.search;

import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

import jp.co.ns_sol.asase.office.common.book.BookErrorCode;
import jp.co.ns_sol.asase.office.model.book.BookBean;
import jp.co.ns_sol.asase.office.model.book.BookPool;
import jp.co.ns_sol.asase.office.model.book.BooksBean;
import jp.co.ns_sol.sysrdc.abs.common.AbsException;
import jp.co.ns_sol.sysrdc.abs.control.AbsServlet;
import jp.co.ns_sol.sysrdc.abs.control.Forwarder;
import jp.co.ns_sol.sysrdc.abs.control.MeCabServlet;
import jp.co.ns_sol.sysrdc.abs.control.ServletConstants;
import jp.co.ns_sol.sysrdc.abs.control.ServletUtility;
/**
 * ���Ж��⊮�pservlet
 * 
 * �����F
 * 
 * @author ��
 *
 */
public class CompleteBookServlet extends AbsServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public CompleteBookServlet(){
		super();
	}
	public void execute(
	        HttpServletRequest request,
	        HttpServletResponse response)
	        throws javax.servlet.ServletException, java.io.IOException {
			
	        Forwarder forwarder = createForwarder(request, response);
	        HttpSession session = request.getSession(true);
	        
	        //���Џ��ۑ��p
	        Integer price = null;
	        String isbn = null;
	        String author = null;
	        String title = null;
	        String translator = null;
	        String cover = null;
	        int indexOfTempBooksBean=0;
	        
	        ////
	        BookBean tempBookBean=new BookBean();
	        BooksBean tempBooksBean = new BooksBean();
	        BooksBean books=null;
	        
	        //����keyword���擾����    	        	           	        
	        String keyword = java.net.URLDecoder.decode(request.getParameter("compstr"),"UTF-8");
	        
	        //�S���̖{���擾
	        BookPool pool = BookPool.getInstance(session);
	        try {
	             books =
	                pool.select(
	                    isbn,
	                    author,
	                    price,
	                    title,
	                    translator,
	                    cover	          
	                    );
	            
	        } catch (AbsException ex) {
	            forwarder.addException(ex);
	        }
	        if (forwarder.hasException()) {
	            return;
	        }
	        //printwiter 
	        PrintWriter out = response.getWriter();
	        int count=0;
	        for(int i=0;i<books.getLength();i++){
	    	   //���S��v�Ō�������@�啶����ʂ��Ȃ��E�E�E�E�E�E�E/////
	    	   tempBookBean=books.getBean(i);
	    	   if(tempBookBean.getTitle().toLowerCase().indexOf(keyword.toLowerCase())!=-1){
	    		   tempBooksBean.setBean(indexOfTempBooksBean,tempBookBean);
	    		   indexOfTempBooksBean++;
	    		   out.print(tempBookBean.getTitle()+"");
	    		   if(count>=7){
	    			   break;
	    		   }
	    		   out.print(",");
	    		   count++;
	    	   }
	    	   
	        }	
	        count=0;
	    }
	
}

