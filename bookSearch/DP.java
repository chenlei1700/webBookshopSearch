package jp.co.ns_sol.sysrdc.abs.search;
/**
 * 連続DPによる文字列間距離の算出
 * @param strReerencef 形態素データベース側文字列
 * @param strTarget ユーザ入力側文字列
 * @return 計算コスト
 */
public class DP {
	/**
	 * 連続DPによる文字列間距離の算出
	 * @param strReerencef 形態素データベース側文字列
	 * @param strTarget ユーザ入力側文字列
	 * @return 計算コスト
	 */
	public static int calcCost(String strReference, String strTarget) {

		/* デバッグモードの場合，ログを標準出力します． */
		final boolean DEBUG_MODE = false;

		/* 距離を正規化するか */
		final boolean NORMALIZE_DISTANCE = false;

		/* 傾斜制限 */
		/*
		 * ●へ遷移可能なノードは○のみ．
		 *
		 * ==参考図==
		 *     ●
		 * ○○
		 *   ○
		 */

		/* 入力文字列の長さを取得 */
		int strRefLen = strReference.length();
		int strTarLen = strTarget.length();

		/* 文字列の長さでtargetとreferenceの文字列を入れ替え */
		String strShort = strTarget;
		String strLong  = strReference;
		if( strRefLen < strTarLen ) {
			strShort = strReference;
			strLong  = strTarget;
		}
		int strShortLen = strShort.length();
		int strLongLen = strLong.length();

		/* 累積コストテーブル */
		int[][] assumCost = new int[strShortLen+1][strLongLen];

		/* 経路コストテーブル */
		int[][] pathLength = new int[strShortLen+1][strLongLen];

		/* バックトラックテーブル */
		int[][][] backtrack = new int[strShortLen+1][strLongLen][2];	//2:[j|i]

		/* 各種テーブルの初期化 */
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

		/* ワードスポッティング（始点・終点フリーDP Matching） */
		for(int i=1; i<strLongLen; i++) {

			for(int j=1; j<strShortLen+1; j++) {

				/* 傾斜制限のなかで最小コストを求める */
				/* ==参考図==
				 *     ●
				 * ①②
				 *   ③
				 */
				int localDistance;
				int currentIAtStr = i;
				int currentJAtStr = j-1;
				int minCost = 100000;
				int minNode = -1;
				for(int preNode=1; preNode<=3; preNode++) {
					int _assumCost = 100000;
					switch(preNode) {
					case 1:	//①
						if( !(j-1>=0 && i-2>=0) ) continue;
						localDistance = calcLocalDistance(strShort.charAt(currentJAtStr), strLong.charAt(currentIAtStr))
							+ 2 * calcLocalDistance(strShort.charAt(currentJAtStr), strLong.charAt(currentIAtStr - 1));
						_assumCost = assumCost[j-1][i-2] + localDistance;
						break;
					case 2:	//②
						localDistance = calcLocalDistance(strShort.charAt(currentJAtStr), strLong.charAt(currentIAtStr));
						_assumCost = assumCost[j-1][i-1] + localDistance;
						break;
					case 3:	//③
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

				/* 累積コストの更新 */
				assumCost[j][i] = minCost;

				/* 経路コストテーブルとバックトラックテーブルの更新 */
				switch(minNode) {
				case 1:	//①
					pathLength[j][i] =pathLength[j-1][i-2] + 3;
					backtrack[j][i]   = new int[]{j, i-1};
					backtrack[j][i-1] = new int[]{j-1, i-2};
					break;
				case 2:	//②
					pathLength[j][i] =pathLength[j-1][i-1] + 2;
					backtrack[j][i]   = new int[]{j-1, i-1};
					break;
				case 3:	//③
					pathLength[j][i] =pathLength[j-2][i-1] + 3;
					backtrack[j][i]   = new int[]{j-1, i};
					backtrack[j-1][i] = new int[]{j-2, i-1};
				default:	//①
					break;
				}
			}
		}

		/* 累積コスト最小値を求める． */
		int minAssumCost = 100000;
		int minAssumEndPos = -1;
		if( NORMALIZE_DISTANCE ) {
			/* 正規化累積コストで最小値を求める */
			for(int i=0; i<strLongLen; i++) {
				if( minAssumCost>(double)assumCost[strShortLen][i]/(double)pathLength[strShortLen][i] ) {
					minAssumCost = (int)(((double)assumCost[strShortLen][i]/(double)pathLength[strShortLen][i]) * 1000);
					minAssumEndPos = i;
				}
			}
		} else {
			/* 正規化累積コストで最小値を求める */
			for(int i=0; i<strLongLen; i++) {
				if( minAssumCost>assumCost[strShortLen][i] ) {
					minAssumCost = assumCost[strShortLen][i];
					minAssumEndPos = i;
				}
			}
		}

		/* forDebug 累積コストテーブル書き出し */
		if( DEBUG_MODE ) {
			System.out.println("\n累積コストテーブル：");
			for(int j=0; j<strShortLen; j++) {
				for(int i=0; i<strLongLen; i++) {
					System.out.print(assumCost[j][i] + "\t");
				}
				System.out.println();
			}
		}

		/* バックトラック */
		int backtrackPos = minAssumEndPos;
		int minAssumStartPos = 0;
		if( DEBUG_MODE ) {
			System.out.println("\nバックトラック結果：");
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

		/* strRef（ユーザ入力）の長さがstrTar（形態素DB要素）の長さよりも長い場合は，
		 * length(strRef∪strTar) - length(strRef∩strTar)をペナルティに与える． */
		if( strRefLen > strTarLen ) {
			if( DEBUG_MODE ) {
				System.out.printf("minAssumStartPos:%d, strRefLen:%d, minAssumEndPos:%d, backtrack[strShortLen][minAssumEndPos][1]:%d\n", minAssumStartPos, strRefLen, minAssumEndPos, backtrack[strShortLen][minAssumEndPos][1]);
			}
			minAssumCost += (backtrack[1][minAssumStartPos][1] - 0) + ((strRefLen-1) - backtrack[strShortLen][minAssumEndPos][1]);
		}

		return minAssumCost;
	}

	/**
	 * 文字aと文字bが一致するなら0を，一致しないなら1を返却します．
	 * @param a リテラル
	 * @param b リテラル
	 * @return 入力文字間の距離（0 or 1）
	 */
	public static int calcLocalDistance(char a, char b) {
		return a==b ? 0 : 1;
	}
}
