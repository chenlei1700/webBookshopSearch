package jp.co.ns_sol.sysrdc.abs.control;

import java.util.ArrayList;
import java.util.List;

import net.moraleboost.mecab.Node;
import net.moraleboost.mecab.impl.StandardLattice;
import net.moraleboost.mecab.impl.StandardTagger;

public class MeCabServlet {
	public static List<String> parse(String str){
		List<String> arry=new ArrayList<String>();
	
		// Taggerを構築。
	    // 引数には、MeCabのcreateTagger()関数に与える引数を与える。
	    StandardTagger tagger = new StandardTagger("-Oyomi");
	
	    // バージョン文字列を取得
	    //System.out.println("MeCab version " + tagger.version());
	
	    // Lattice（形態素解析に必要な実行時情報が格納されるオブジェクト）を構築
	    StandardLattice lattice = tagger.createLattice();
	
	    // 解析対象文字列をセット
	    
	    lattice.setSentence(str);
	
	    // tagger.parse()を呼び出して、文字列を形態素解析する。
	    tagger.parse(lattice);
	

	
	    // 一つずつ形態素をたどりながら、表層形と素性を出力
	   
	    Node node=lattice.bosNode();
	    while (node != null) {
	        String surface = node.surface();
	        arry.add(surface);
	        node = node.next();
	    }
	
	    // lattice, taggerを破壊
	    lattice.destroy();
	    tagger.destroy();
	   
	    return arry;
	}
	

}
