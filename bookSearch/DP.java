package jp.co.ns_sol.sysrdc.abs.search;
/**
 * �A��DP�ɂ�镶����ԋ����̎Z�o
 * @param strReerencef �`�ԑf�f�[�^�x�[�X��������
 * @param strTarget ���[�U���͑�������
 * @return �v�Z�R�X�g
 */
public class DP {
	/**
	 * �A��DP�ɂ�镶����ԋ����̎Z�o
	 * @param strReerencef �`�ԑf�f�[�^�x�[�X��������
	 * @param strTarget ���[�U���͑�������
	 * @return �v�Z�R�X�g
	 */
	public static int calcCost(String strReference, String strTarget) {

		/* �f�o�b�O���[�h�̏ꍇ�C���O��W���o�͂��܂��D */
		final boolean DEBUG_MODE = false;

		/* �����𐳋K�����邩 */
		final boolean NORMALIZE_DISTANCE = false;

		/* �X�ΐ��� */
		/*
		 * ���֑J�ډ\�ȃm�[�h�́��̂݁D
		 *
		 * ==�Q�l�}==
		 *     ��
		 * ����
		 *   ��
		 */

		/* ���͕�����̒������擾 */
		int strRefLen = strReference.length();
		int strTarLen = strTarget.length();

		/* ������̒�����target��reference�̕���������ւ� */
		String strShort = strTarget;
		String strLong  = strReference;
		if( strRefLen < strTarLen ) {
			strShort = strReference;
			strLong  = strTarget;
		}
		int strShortLen = strShort.length();
		int strLongLen = strLong.length();

		/* �ݐσR�X�g�e�[�u�� */
		int[][] assumCost = new int[strShortLen+1][strLongLen];

		/* �o�H�R�X�g�e�[�u�� */
		int[][] pathLength = new int[strShortLen+1][strLongLen];

		/* �o�b�N�g���b�N�e�[�u�� */
		int[][][] backtrack = new int[strShortLen+1][strLongLen][2];	//2:[j|i]

		/* �e��e�[�u���̏����� */
		for(int i=0; i<strLongLen; i++) {
			assumCost[0][i] = 0;
			pathLength[0][i] = 0;
		}
		for(int j=2; j<strShortLen+1; j++) {
			assumCost[j][0] = 100000;
			pathLength[j][0] = 100000;
		}
		assumCost[1][0]  = calcLocalDistance(strLong.charAt(0), strShort.charAt(0));
		pathLength[1][0] = 1;
		backtrack[1][0]  = new int[]{0, 0};

		/* ���[�h�X�|�b�e�B���O�i�n�_�E�I�_�t���[DP Matching�j */
		for(int i=1; i<strLongLen; i++) {

			for(int j=1; j<strShortLen+1; j++) {

				/* �X�ΐ����̂Ȃ��ōŏ��R�X�g�����߂� */
				/* ==�Q�l�}==
				 *     ��
				 * �@�A
				 *   �B
				 */
				int localDistance;
				int currentIAtStr = i;
				int currentJAtStr = j-1;
				int minCost = 100000;
				int minNode = -1;
				for(int preNode=1; preNode<=3; preNode++) {
					int _assumCost = 100000;
					switch(preNode) {
					case 1:	//�@
						if( !(j-1>=0 && i-2>=0) ) continue;
						localDistance = calcLocalDistance(strShort.charAt(currentJAtStr), strLong.charAt(currentIAtStr))
							+ 2 * calcLocalDistance(strShort.charAt(currentJAtStr), strLong.charAt(currentIAtStr - 1));
						_assumCost = assumCost[j-1][i-2] + localDistance;
						break;
					case 2:	//�A
						localDistance = calcLocalDistance(strShort.charAt(currentJAtStr), strLong.charAt(currentIAtStr));
						_assumCost = assumCost[j-1][i-1] + localDistance;
						break;
					case 3:	//�B
						if( !(j-2>=0 && i-1>=0) ) continue;
						localDistance = calcLocalDistance(strShort.charAt(currentJAtStr), strLong.charAt(currentIAtStr))
								+ 2 * calcLocalDistance(strShort.charAt(currentJAtStr - 1), strLong.charAt(currentIAtStr));
						_assumCost = assumCost[j-2][i-1] + localDistance;
						break;
					default:
						break;
					}

					if( _assumCost < minCost ) {
						minCost = _assumCost;
						minNode = preNode;
					}
				}

				/* �ݐσR�X�g�̍X�V */
				assumCost[j][i] = minCost;

				/* �o�H�R�X�g�e�[�u���ƃo�b�N�g���b�N�e�[�u���̍X�V */
				switch(minNode) {
				case 1:	//�@
					pathLength[j][i] =pathLength[j-1][i-2] + 3;
					backtrack[j][i]   = new int[]{j, i-1};
					backtrack[j][i-1] = new int[]{j-1, i-2};
					break;
				case 2:	//�A
					pathLength[j][i] =pathLength[j-1][i-1] + 2;
					backtrack[j][i]   = new int[]{j-1, i-1};
					break;
				case 3:	//�B
					pathLength[j][i] =pathLength[j-2][i-1] + 3;
					backtrack[j][i]   = new int[]{j-1, i};
					backtrack[j-1][i] = new int[]{j-2, i-1};
				default:	//�@
					break;
				}
			}
		}

		/* �ݐσR�X�g�ŏ��l�����߂�D */
		int minAssumCost = 100000;
		int minAssumEndPos = -1;
		if( NORMALIZE_DISTANCE ) {
			/* ���K���ݐσR�X�g�ōŏ��l�����߂� */
			for(int i=0; i<strLongLen; i++) {
				if( minAssumCost>(double)assumCost[strShortLen][i]/(double)pathLength[strShortLen][i] ) {
					minAssumCost = (int)(((double)assumCost[strShortLen][i]/(double)pathLength[strShortLen][i]) * 1000);
					minAssumEndPos = i;
				}
			}
		} else {
			/* ���K���ݐσR�X�g�ōŏ��l�����߂� */
			for(int i=0; i<strLongLen; i++) {
				if( minAssumCost>assumCost[strShortLen][i] ) {
					minAssumCost = assumCost[strShortLen][i];
					minAssumEndPos = i;
				}
			}
		}

		/* forDebug �ݐσR�X�g�e�[�u�������o�� */
		if( DEBUG_MODE ) {
			System.out.println("\n�ݐσR�X�g�e�[�u���F");
			for(int j=0; j<strShortLen; j++) {
				for(int i=0; i<strLongLen; i++) {
					System.out.print(assumCost[j][i] + "\t");
				}
				System.out.println();
			}
		}

		/* �o�b�N�g���b�N */
		int backtrackPos = minAssumEndPos;
		int minAssumStartPos = 0;
		if( DEBUG_MODE ) {
			System.out.println("\n�o�b�N�g���b�N���ʁF");
		}
		for(int j=strShortLen, i=backtrackPos; j>=1;) {
			if( DEBUG_MODE ) {
				System.out.printf("(%02d,%02d)\t(%s,%s)\n", j,i,strShort.charAt(j-1),strLong.charAt(i));
			}
			if( j==1 ) break;
			j = backtrack[j][i][0];
			i = backtrack[j][i][1];
			minAssumStartPos = i;
		}

		/* strRef�i���[�U���́j�̒�����strTar�i�`�ԑfDB�v�f�j�̒������������ꍇ�́C
		 * length(strRef��strTar) - length(strRef��strTar)���y�i���e�B�ɗ^����D */
		if( strRefLen > strTarLen ) {
			if( DEBUG_MODE ) {
				System.out.printf("minAssumStartPos:%d, strRefLen:%d, minAssumEndPos:%d, backtrack[strShortLen][minAssumEndPos][1]:%d\n", minAssumStartPos, strRefLen, minAssumEndPos, backtrack[strShortLen][minAssumEndPos][1]);
			}
			minAssumCost += (backtrack[1][minAssumStartPos][1] - 0) + ((strRefLen-1) - backtrack[strShortLen][minAssumEndPos][1]);
		}

		return minAssumCost;
	}

	/**
	 * ����a�ƕ���b����v����Ȃ�0���C��v���Ȃ��Ȃ�1��ԋp���܂��D
	 * @param a ���e����
	 * @param b ���e����
	 * @return ���͕����Ԃ̋����i0 or 1�j
	 */
	public static int calcLocalDistance(char a, char b) {
		return a==b ? 0 : 1;
	}
}
