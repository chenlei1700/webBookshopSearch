package jp.co.ns_sol.sysrdc.abs.search;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import net.moraleboost.mecab.Node;
import net.moraleboost.mecab.impl.StandardLattice;
import net.moraleboost.mecab.impl.StandardTagger;
/**
 * 引数の文字列を形態素解析し，その結果を形態素のArrayListとして返却する．
 * @param 文字列
 * @return 形態素のArrayList
 */
public class MorphemeParse {
	/**
	 * 引数の文字列を形態素解析し，その結果を形態素のArrayListとして返却する．
	 * @param 文字列
	 * @return 形態素のArrayList
	 */
	public static ArrayList<MorphemeNameAndKana> parse(String str){

		/* 形態素解析結果格納 */
		ArrayList<MorphemeNameAndKana> result = new ArrayList<MorphemeNameAndKana>();

		/* Taggerを構築
		 * 引数には、MeCabのcreateTagger()関数に与える引数を与える。*/
	    StandardTagger tagger = new StandardTagger("-Oyomi");

	    /* Lattice（形態素解析に必要な実行時情報が格納されるオブジェクト）を構築 */
	    StandardLattice lattice = tagger.createLattice();

	    /* 解析対象文字列をセット */
	    lattice.setSentence(str);

	    /* tagger.parse()を呼び出して、文字列を形態素解析する */
	    tagger.parse(lattice);

	    /* 一つずつ形態素をたどりながら、表層形と素性を出力 */
	    Node node=lattice.bosNode();
	    while (node != null) {

	    	/* 形態素と品詞の取得 */
	    	String name = node.surface();
	    	String wordClass = regexMatch(node.feature(), "^[^,]*");

	    	/* 形態素の品詞が助詞でないならば追加 */
	    	if( !( wordClass.equals("助詞") || wordClass.equals("BOS/EOS") || name.matches(".*'.*") || name.matches(".*\".*") || name.equals("") ) ) {
	    		MorphemeNameAndKana mnk = new MorphemeNameAndKana();
	    		String kana = regexMatch(node.feature(), "[^,]*?$");

	    		/* 読み仮名が存在しない場合 */
	    		if( kana.equals("*") ) {
	    			mnk.name = name;
	    			mnk.kana = "*";
	    		}
	    		else {
	    			mnk.name = name;
	    			mnk.kana =kana;
	    		}

	    		result.add(mnk);
	    		node = node.next();
	    	} else {
	    		node = node.next();
	    	}
	    }

	    /* lattice, taggerを破棄 */
	    lattice.destroy();
	    tagger.destroy();

	    return result;
	}
	/**
	 * 入力文字列に対して正規表現にマッチした部分文字列を返却します．
	 * @param input. regex対象の文字列．
	 * @param regex. 正規表現の文字列．
	 * @return 正規表現にマッチした文字列．
	 */
    public static String regexMatch(String input, String regex) {

		/* 正規表現をコンパイル */
		Pattern pattern = Pattern.compile(regex);

		/* 正規表現にマッチする文字列を取得 */
		Matcher matcher = pattern.matcher(input);

		/* 結果の返却 */
		if( matcher.find() ) {
			return matcher.group();
		}
		else {
			return "";
		}
	}

}
