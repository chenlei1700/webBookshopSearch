package jp.co.ns_sol.sysrdc.abs.search;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.sql.SQLException;

import jp.co.ns_sol.asase.office.model.book.BookBean;
import jp.co.ns_sol.asase.office.model.book.BookPool;
import jp.co.ns_sol.asase.office.model.book.BooksBean;
import jp.co.ns_sol.asase.office.model.morpheme.MorphemeBean;
import jp.co.ns_sol.asase.office.model.morpheme.MorphemePool;
import jp.co.ns_sol.asase.office.model.morpheme.MorphemesBean;
import jp.co.ns_sol.asase.office.model.morpheme_list.MorphemeListBean;
import jp.co.ns_sol.asase.office.model.morpheme_list.MorphemeListPool;
import jp.co.ns_sol.asase.office.model.morpheme_list.MorphemeListsBean;
import jp.co.ns_sol.sysrdc.abs.common.AbsException;
import jp.co.ns_sol.sysrdc.abs.control.AbsServlet;
import jp.co.ns_sol.sysrdc.abs.control.Forwarder;
import jp.co.ns_sol.sysrdc.abs.control.ServletUtility;
/**
 * もしかして検索用servlet
 * 
 * @author 陳雷
 *
 */

public class SuggesBookServlet extends AbsServlet {
	
	private static final long serialVersionUID = 1L;
	
	public SuggesBookServlet() {
        super();
    }

    /**
     * このサーブレットに固有の処理を行います。
     * @throws AbsException 
     * @throws ServletException
     * @throws IOException
     * @throws SQLException
     */
    public void execute(
        HttpServletRequest request,
        HttpServletResponse response)
        throws javax.servlet.ServletException, java.io.IOException,SQLException, AbsException {
    	System.out.println("susgest begin   ---");
        Forwarder forwarder = createForwarder(request, response);
        HttpSession session = request.getSession(true);
        
        //3d回転表示部分のapplicationにあるregexlist属性の設定
        String type = ServletUtility.getStringParameter(request, "type");
        String regex = null; 
        if(type.equals("tag")){
        	regex = java.net.URLDecoder.decode(request.getParameter("comp"),"UTF-8");
        }else{
        	regex = ServletUtility.getStringParameter(request, "comp");
        }
        ServletContext application = request.getSession().getServletContext();
        Queue<String> regexlist =  (Queue<String>) application.getAttribute("regexlist");
         
        if(regexlist==null){
        	regexlist =  new LinkedList<String>();
        }
        if(regexlist.size()>50){regexlist.poll();}
        if(!regexlist.contains(regex))	regexlist.offer(regex);
        application.setAttribute("regexlist", regexlist);
        
    
        new DBinitial();
        Connection conn = DBinitial.connection;
        
	  	//書籍情報変数
        Integer price = null;
        String isbn = null;
        String author = null;
        String translator = null;
        String cover = null;
        String title = null;
        int seq_publisher_id;
        int pos;
        //database接続用変数
        Statement stmt = null;
        ResultSet rset = null;
  	  	boolean isFromParti = false;
  	   
  	  	//書籍保存用変数
  	  	List<BookBean> beans= new ArrayList<BookBean>();
  	  	BookBean bookbean = new BookBean();
  	  	BooksBean books=new BooksBean();
  	  	BooksBean booksbean = new BooksBean();
  	  	
  	  	MorphemeListPool morphemeListpool = MorphemeListPool.getInstance(session);
  	  	MorphemeListsBean morphemeListsBean = new MorphemeListsBean();
  	  	MorphemeListBean morphemeListBean = new MorphemeListBean();
  	  
  	  	MorphemePool morphemepool = MorphemePool.getInstance(session);
  	  	MorphemesBean morphemesbean = new MorphemesBean();
  	  	MorphemeBean morphemebean = new MorphemeBean();
  	  	
  	  	//keyword抽出
  	  	String stringOfKeyword = null;
  		if (request.getSession().getAttribute("status111") != null){
  	  		stringOfKeyword = (String) request.getSession().getAttribute("keyword");
  	  		isFromParti = true;
  	  	}
  		else{
  			stringOfKeyword = java.net.URLDecoder.decode(request.getParameter("comp").trim(),"UTF-8");
  		}
  	  
  	  	
  	  	request.getSession().setAttribute("status111",null);
  	  	
  	  	System.out.println(stringOfKeyword);
  	  	
  	  	//空白がある場合PartialMatchBookServletに投げる
  	  	if((stringOfKeyword.indexOf(" ")!=-1||stringOfKeyword.indexOf("　")!=-1)&&isFromParti != true){
  	  		request.getSession().setAttribute("keyword", stringOfKeyword);
  	  		
  	  		System.out.println(stringOfKeyword+"********");
  	  		System.out.println("to partital");
    	
    	
	    	forwarder.forward("/book/PartialMatchBookServlet");//break;
	    
	    	return;
	    
	    }
  	  	//空白がない場合検索する　ここから本の検索手順
	    else{
	    	isFromParti = false;
	    	System.out.println(stringOfKeyword);
	    	//keywordから解析した形態素を保存する（名前、カナ）変数
		    MorphemeNameAndKana morphemeNameAndKana = null;
		    String temparry_name = null;
		    String temparry_kana = null;
		    
	        ArrayList<MorphemeNameAndKana> arrylist =  MorphemeParse.parse(stringOfKeyword);//morphemeNameAndKanaの配列
		    Iterator<MorphemeNameAndKana> it = arrylist.iterator();//morphemeNameAndKanaのiterator
		    
		    String str = null;//形態素保存用
		    
		    int seq_morpheme_list_id;//morpheme_listのpkを保存する変数
		    String isbn_seq = null;//書籍のisbnを保存する変数
		    
		    ArrayList<String> compareStringArry = new ArrayList<String>();//isbn_seqを格納する、mapのソート順にisbnを格納する
		    final HashMap<String,Integer> map = new HashMap<String, Integer>();//key->isbn value->形態素が出現する回数
		    int amount;//回数統計用
		    
		    	//arrylistにある形態素を一つずつ出して比較する
			    for(int i = 0;i < arrylist.size();i++){
			    	//入力した形態素がカナか漢字かの判断。カナの場合はmorpheme_listに漢字がある場合漢字に変換
			    	morphemeNameAndKana=it.next();
			    	temparry_name = new String(morphemeNameAndKana.name);
			    	temparry_kana = new String(morphemeNameAndKana.kana);
			    	
			    	if(temparry_kana=="*"||temparry_kana.equals(temparry_name)==false){//kanaであるかどうかの判断
			    		morphemeListsBean = morphemeListpool.select(null,null,temparry_name); 
				    	
			    		if(morphemeListsBean.getLength()!=0){
			    			morphemeListBean = morphemeListsBean.getBean(0);
			    			str=morphemeListBean.getMorphemeName();
			    		}
			    		else str = temparry_name;	
			    	}
			    	else str = temparry_name;
			    	
			    	
			    	//morpheme_list DBに　ｓｔｒ検索し、ＰＫをとる
			    	stmt = conn.createStatement();	
			    	rset = stmt.executeQuery("SELECT seq_morpheme_list_id FROM morpheme_list WHERE morpheme_name like '%"+ str +"%'");				    		
			    	
			    	while(rset.next()){	
			    		seq_morpheme_list_id = rset.getInt("seq_morpheme_list_id");
				    	morphemesbean = morphemepool.select(null,null,null,seq_morpheme_list_id);
					    	
				   		for(int k = 0;k < morphemesbean.getLength();k++){
			    			isbn_seq = morphemesbean.getBean(k).getIsbn();
			    			pos = 100-morphemesbean.getBean(k).getPosition();
			    			if(!compareStringArry.contains(isbn_seq)){//compareIntArryにisbn_seqを入れる、sortのindexとして
				    			compareStringArry.add(isbn_seq);
				    			map.put(isbn_seq, 1+pos);//mapにisbn_seq　と　数　を格納
				    		}
			    			else{
				    			amount = map.get(isbn_seq);
				    			map.put(isbn_seq, amount+1+pos);
				    		}
			    		}	
			    	}
			    	
			    	//mapをソートする
			    	Collections.sort(compareStringArry,new Comparator<Object>(){
			    	    
			              public int compare(Object o1,Object o2){
			            	  //降順でソート
			                  if(Double.parseDouble(map.get(o1).toString())<Double.parseDouble(map.get(o2).toString()))
			                     	return 1;
			                  else if(Double.parseDouble(map.get(o1).toString())==Double.parseDouble(map.get(o2).toString()))
			                  		return 0;                  
			                  else
			                      return -1;
			                  }
			              }
			        );
			    	
			    }
			    if(stmt!=null)stmt.close();
			    
			    //ここから検索結果がない場合のもしかして検索
			    if(compareStringArry.size()==0){
			    	arrylist =  MorphemeParse.parse(stringOfKeyword);
			    	it = arrylist.iterator();
			    	
			    	BookPool pool = BookPool.getInstance(session);
			    	booksbean = pool.select("", "", null, "", "", "");//全部の本を取得する
			    	
			    	int arryListSize = arrylist.size();
			    	for(int j = 0;j<arryListSize;j++){
				    	morphemeNameAndKana = it.next();
				    	temparry_name = new String(morphemeNameAndKana.name);
				    	temparry_kana = new String(morphemeNameAndKana.kana);
				    	
				    	if(temparry_kana=="*"||temparry_kana.equals(temparry_name)==false){//kanaであるかどうかの判断
				    		morphemeListsBean = morphemeListpool.select(null,null,temparry_name);
				    		
				    		if(morphemeListsBean.getLength()!=0){
				    			morphemeListBean = morphemeListsBean.getBean(0);
				    			str = morphemeListBean.getMorphemeName();
				    		}
				    		else str = temparry_name;	
				    	}
				    	else str = temparry_name;
				    	
				    	int booksBeanLength = booksbean.getLength();
				    	for( int i = 0; i<booksBeanLength;i++){
				    			bookbean = booksbean.getBean(i);
				    			title = bookbean.getTitle();
				    			isbn_seq = bookbean.getIsbn();
				    			int length = DP.calcCost(str,title);
				    			if(!compareStringArry.contains(isbn_seq)){//compareIntArryにisbn_seqを入れる、sortのindexとして
					    			compareStringArry.add(isbn_seq);
					    			map.put(isbn_seq, 1000-length);//mapにisbn_seq　と　数　を格納
					    		}
				    	}
				    	
				    	Collections.sort(compareStringArry,new Comparator<Object>(){
				    	    
				              public int compare(Object o1,Object o2){
				                 
				                  if(Double.parseDouble(map.get(o1).toString())<Double.parseDouble(map.get(o2).toString()))
				                     	return 1;
				                  
				                  else if(Double.parseDouble(map.get(o1).toString())==Double.parseDouble(map.get(o2).toString()))
				                  		return 0;                  
				                                      
				                  else
				                      return -1;
				                  }
				              }
				        );
			    	}
			    	//compareStringArryにあるisbn順にbooksに保存してforward
			    	int compareStringArrySize = compareStringArry.size();
			    	int booksBeanLength2 = booksbean.getLength();
			    	for(int i = 0; i < compareStringArrySize; i++ ){
			    		for(int j = 0; j < booksBeanLength2; j++){
			    			bookbean = booksbean.getBean(j);
			    			isbn = (String) bookbean.getKey();
			    			if(isbn.equals(compareStringArry.get(i))){
			    				BookBean book = new BookBean();
			    		    	
			    		    	book.setAuthor(bookbean.getAuthor());
			    		    	book.setIsbn(isbn);
			    		    	book.setPrice(bookbean.getPrice());
			    		    	book.setPublisherId(bookbean.getPublisher(session).getPublisherId());
			    		    	book.setTitle(bookbean.getTitle());
			    		    	book.setTranslator(bookbean.getTranslator());
			    		    	book.setCover(bookbean.getCover());	
			    		    	book.setAuthor(bookbean.getAuthor());
			    		    	
			    		    	beans.add(book);
			    			}
			    		}
			    	}
			    	
			    	books = new BooksBean((BookBean[]) beans.toArray(new BookBean[0]));
	
			    	request.setAttribute("books", books);
			    	request.getSession().setAttribute("books", books);
			    	forwarder.forward("/book/BookList.jsp");return;
			    	 
			    }
			    
			    //形態素の検索できた場合のbooks処理
			    int compareStringArrySize = compareStringArry.size();
			    for(int i = 0; i < compareStringArrySize; i++ ){
			    	//morphemeからisbnを取る
			    	
			    	morphemesbean = morphemepool.select(null,null,compareStringArry.get(i),null);
			    	 morphemebean = morphemesbean.getBean(0);
			    	
			    	isbn = morphemebean.getIsbn();
			    	
			    	stmt = conn.createStatement();				    		 
			    	rset = stmt.executeQuery("SELECT distinct * FROM book WHERE isbn= '"+ isbn+"'" );	

			    	if(rset.next()==false){
			    		books= new BooksBean((BookBean[]) beans.toArray(new BookBean[0]));
			    		request.setAttribute("books", books);
			            request.getSession().setAttribute("books", books);
			    		forwarder.forward("/book/BookList.jsp");
			    		return;
			    	}
			    	
			    	isbn = rset.getString("isbn");
			    	author = rset.getString("author");
			    	title = rset.getString("title");
			    	price = rset.getInt("price");
			    	cover = rset.getString("cover");
			    	translator = rset.getString("translator");
			    	seq_publisher_id = rset.getInt("seq_publisher_id");
			    		
			    	stmt.close();
			    	
			    	BookBean book = new BookBean();
			    	
			    	book.setAuthor(author);
			    	book.setIsbn(isbn);
			    	book.setPrice(price);
			    	book.setPublisherId(seq_publisher_id);
			    	book.setTitle(title);
			    	book.setTranslator(translator);
			    	book.setCover(cover);	
			    	beans.add(book);
			    	
			    }
		
		            books= new BooksBean((BookBean[]) beans.toArray(new BookBean[0]));
	
		            request.setAttribute("books", books);
		            request.getSession().setAttribute("books", books);
		            
		        if (forwarder.hasException()) {
		            return;
		        }
	
	       forwarder.forward("/book/BookList.jsp");
		
	    }
    }
    
  
}
