package jp.co.ns_sol.sysrdc.abs.search;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import net.moraleboost.mecab.Node;
import net.moraleboost.mecab.impl.StandardLattice;
import net.moraleboost.mecab.impl.StandardTagger;
/**
 * �����̕�������`�ԑf��͂��C���̌��ʂ��`�ԑf��ArrayList�Ƃ��ĕԋp����D
 * @param ������
 * @return �`�ԑf��ArrayList
 */
public class MorphemeParse {
	/**
	 * �����̕�������`�ԑf��͂��C���̌��ʂ��`�ԑf��ArrayList�Ƃ��ĕԋp����D
	 * @param ������
	 * @return �`�ԑf��ArrayList
	 */
	public static ArrayList<MorphemeNameAndKana> parse(String str){

		/* �`�ԑf��͌��ʊi�[ */
		ArrayList<MorphemeNameAndKana> result = new ArrayList<MorphemeNameAndKana>();

		/* Tagger���\�z
		 * �����ɂ́AMeCab��createTagger()�֐��ɗ^���������^����B*/
	    StandardTagger tagger = new StandardTagger("-Oyomi");

	    /* Lattice�i�`�ԑf��͂ɕK�v�Ȏ��s����񂪊i�[�����I�u�W�F�N�g�j���\�z */
	    StandardLattice lattice = tagger.createLattice();

	    /* ��͑Ώە�������Z�b�g */
	    lattice.setSentence(str);

	    /* tagger.parse()���Ăяo���āA��������`�ԑf��͂��� */
	    tagger.parse(lattice);

	    /* ����`�ԑf�����ǂ�Ȃ���A�\�w�`�Ƒf�����o�� */
	    Node node=lattice.bosNode();
	    while (node != null) {

	    	/* �`�ԑf�ƕi���̎擾 */
	    	String name = node.surface();
	    	String wordClass = regexMatch(node.feature(), "^[^,]*");

	    	/* �`�ԑf�̕i���������łȂ��Ȃ�Βǉ� */
	    	if( !( wordClass.equals("����") || wordClass.equals("BOS/EOS") || name.matches(".*'.*") || name.matches(".*\".*") || name.equals("") ) ) {
	    		MorphemeNameAndKana mnk = new MorphemeNameAndKana();
	    		String kana = regexMatch(node.feature(), "[^,]*?$");

	    		/* �ǂ݉��������݂��Ȃ��ꍇ */
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

	    /* lattice, tagger��j�� */
	    lattice.destroy();
	    tagger.destroy();

	    return result;
	}
	/**
	 * ���͕�����ɑ΂��Đ��K�\���Ƀ}�b�`���������������ԋp���܂��D
	 * @param input. regex�Ώۂ̕�����D
	 * @param regex. ���K�\���̕�����D
	 * @return ���K�\���Ƀ}�b�`����������D
	 */
    public static String regexMatch(String input, String regex) {

		/* ���K�\�����R���p�C�� */
		Pattern pattern = Pattern.compile(regex);

		/* ���K�\���Ƀ}�b�`���镶������擾 */
		Matcher matcher = pattern.matcher(input);

		/* ���ʂ̕ԋp */
		if( matcher.find() ) {
			return matcher.group();
		}
		else {
			return "";
		}
	}

}
