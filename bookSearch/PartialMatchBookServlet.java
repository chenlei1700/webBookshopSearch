package jp.co.ns_sol.sysrdc.abs.search;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jp.co.ns_sol.asase.office.model.book.BookBean;
import jp.co.ns_sol.asase.office.model.book.BooksBean;
import jp.co.ns_sol.sysrdc.abs.control.AbsServlet;
import jp.co.ns_sol.sysrdc.abs.control.Forwarder;
/**
 * �󔒂ŋ�؂镶���񌟍��pservlet
 * 
 * @author ��
 *
 */
public class PartialMatchBookServlet extends AbsServlet{
	
	private static final long serialVersionUID = 1L;

	public PartialMatchBookServlet() {
	        super();
	    }

	    /**
	     * ���̃T�[�u���b�g�ɌŗL�̏������s���܂��B
	     * @throws SQLException 
	     */
    public void execute(
	        HttpServletRequest request,
	        HttpServletResponse response)
	        throws javax.servlet.ServletException, java.io.IOException, SQLException {
	    	
    		System.out.println("part begin!!!!");
	        
    		Forwarder forwarder = createForwarder(request, response);
	        HttpSession session = request.getSession(true);
	        
	        ////////////////////�N���X������
	        //database�����ݒ�
		  	  String user = "qito";
		  	  
		  	  String pass = "111";
		  	  
		  	  String servername = "127.0.0.1";
		  	  
		  	  String dbname = "book";
		  	try {
				Class.forName ("org.postgresql.Driver");
			} catch (ClassNotFoundException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		  	Connection conn = null;
		  	try {
				conn=DriverManager.getConnection 
						("jdbc:postgresql://" + servername + ":5432/" + dbname,user,pass);
			} catch (SQLException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		  	 
	        Statement stmt = null;
	        ResultSet rset = null;
	        //���Еۑ��p�ϐ�
	        List<BookBean> beans= new ArrayList<BookBean>();
	        BooksBean books = new BooksBean();
	        
	        String title = null;//title�ۑ��p
	        String keyword = java.net.URLDecoder.decode(request.getSession().getAttribute("keyword").toString(),"UTF-8");
	        
	        //////////arrylist ni
	        String[] keyWordList =new String[30];//�󔒂ŋ�؂�ꂽ������ۑ��p
	        //keyWordList������
	        for(int i = 0; i<10 ; i++){
	        	keyWordList[i]="";
	        }
	        
	        
	        try{
	        	keyWordList=keyword.split("[ �@]");//�󔒂�����
	        	//���K�\����keyWordList[0]��book�f�[�^�x�[�X�Ɍ���
	        	
	        	stmt = conn.createStatement();
	        	rset=stmt.executeQuery("SELECT distinct * FROM book WHERE title like '%"+ keyWordList[0] +"%'");
	        	
	        	//keyWordList�ɂ��镶�����S���܂ޖ{������
	        	while(rset.next()){
	        	
		        	title=rset.getString("title");
		        	int total=0;
		        	for(int i=0;i<keyWordList.length;i++){
		        		if(title.indexOf(keyWordList[i])!=-1)
		        			total++;
		        	}
		        	if(total==keyWordList.length) {
		        		BookBean book = new BookBean();
		        		
		        		book.setPrice(rset.getInt("price"));
		        		book.setIsbn(rset.getString("isbn"));
		        		book.setAuthor(rset.getString("author"));
		        		book.setPublisherId(rset.getInt("seq_publisher_id"));
		        		book.setTranslator(rset.getString("translator"));
		        		book.setCover(rset.getString("cover"));
		        		book.setTitle(rset.getString("title"));
		        		beans.add(book);
		        	}
	        	}
		        
	        	if(stmt!=null)stmt.close();
		        
		        books= new BooksBean((BookBean[]) beans.toArray(new BookBean[0]));
		        
		        request.setAttribute("books", books);
		        request.getSession().setAttribute("books", books);
		        if(books.getLength()==0){
		        	request.getSession().setAttribute("keyword",keyword);
		        	request.getSession().setAttribute("status111",keyword);
		        	System.out.println("to suggest");
		        	forwarder.forward("/book/SuggesBookServlet");
		        	
		        	return;
		        }
	        }
	        
	        catch (SQLException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
	       
	       forwarder.forward("/book/BookList.jsp");
	    }

}
