package jp.co.ns_sol.sysrdc.abs.control;

import java.util.ArrayList;
import java.util.List;

import net.moraleboost.mecab.Node;
import net.moraleboost.mecab.impl.StandardLattice;
import net.moraleboost.mecab.impl.StandardTagger;

public class MeCabServlet {
	public static List<String> parse(String str){
		List<String> arry=new ArrayList<String>();
	
		// Tagger���\�z�B
	    // �����ɂ́AMeCab��createTagger()�֐��ɗ^���������^����B
	    StandardTagger tagger = new StandardTagger("-Oyomi");
	
	    // �o�[�W������������擾
	    //System.out.println("MeCab version " + tagger.version());
	
	    // Lattice�i�`�ԑf��͂ɕK�v�Ȏ��s����񂪊i�[�����I�u�W�F�N�g�j���\�z
	    StandardLattice lattice = tagger.createLattice();
	
	    // ��͑Ώە�������Z�b�g
	    
	    lattice.setSentence(str);
	
	    // tagger.parse()���Ăяo���āA��������`�ԑf��͂���B
	    tagger.parse(lattice);
	

	
	    // ����`�ԑf�����ǂ�Ȃ���A�\�w�`�Ƒf�����o��
	   
	    Node node=lattice.bosNode();
	    while (node != null) {
	        String surface = node.surface();
	        arry.add(surface);
	        node = node.next();
	    }
	
	    // lattice, tagger��j��
	    lattice.destroy();
	    tagger.destroy();
	   
	    return arry;
	}
	

}
