package bcmp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class BCMP_main {

	//課題1 クラス間移動がない場合、α11=1、α21=1と置かないといけない
	//今はalpha2として直接取り込んでいる
	//課題2 クラス数が2で固定
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int N = 10, K = 12, c = 2;
		int node_index[] = {14,15,17,18,21,24,27,29,31,34};
		int nc[] = {5,5};//各クラスの最大値
		double mu[][] = {{5,5,10,5,5,5,5,5,5,10,5,10}, {5,5,10,5,5,5,5,5,5,10,5,10}};//サービス率
		double mu_sim[] = {5,5,10,5,5,5,5,5,5,10,5,10,5,5,10,5,5,5,5,5,5,10,5,10};//シミュレーション用サービス率
		double [][]r = new double[K * c][K * c];
		double alpha[] = new double[K * c];//トラフィック方程式の解(α11=1とする)
		double alpha2[][] = new double[c][K];//クラス別2次元配列
		
		//(1) 推移確率行列の取り込み
		BCMP_main bmain = new BCMP_main();
		bmain.getCSV2("csv/transition_class.csv", K, c, r);
		System.out.println("推移確率行列" +Arrays.deepToString(r));
		
		//(2)トラフィック方程式を解く
		double alpha1[] = new double[K * c - 1];//トラフィック方程式α
		double r1[][] = new double[K * c -1][K * c -1];//Rを転置して対角要素を-1、１行と１列要素を削除
		double b1[] = new double[K * c -1]; //Rの１行目の2列目要素からK*c要素まで
		BCMP_lib blib = new BCMP_lib(K,c,N,mu);
		blib.getPretrafiic(r);//行列の変形
		r1 = blib.getR1();
		b1 = blib.getB1();
		alpha1 = blib.calcGauss(r1, b1);//トラフィック方程式
		blib.getAlpha_value(alpha1);//alpha1を元の形に
		alpha = blib.getAlpha();//alpha1次元
		alpha2 = blib.getAlpha_class();//alphaクラス別
		System.out.println("トラフィック方程式解α(クラス別)" +Arrays.deepToString(alpha2));
		
		System.out.println("トラフィック方程式解α2" +Arrays.deepToString(alpha2));
		//double alpha2[][] = {{1.0, 0.9760707373104454, 1.9469520689427025, 0.9502782204518215, 0.9765260877564881, 1.0196937083667734, 1.09062044987315, 1.0166426589311544, 0.9965026346573167, 1.702724752752357, 0.994438760077145, 1.4609981533055358},
		//		{1.0, 0.9760707373104454, 1.9469520689427025, 0.9502782204518215, 0.9765260877564881, 1.0196937083667734, 1.09062044987315, 1.0166426589311544, 0.9965026346573167, 1.702724752752357, 0.994438760077145, 1.4609981533055358}
		//};
		
		//(3)DB連携の準備
		int combination_id = 1, transition_id = 1;
		MySQL mysql = new MySQL(combination_id, transition_id);
		//int combination_number = mysql.getCombinations();
		//System.out.println("Combination_id = "+combination_id+",総数 = "+combination_number);
		//int n_combination_number = mysql.getCombinations(roop, c);
		//System.out.println("Combination_id = "+combination_id+",n = "+roop+",総数 = "+n_combination_number);
		//int value_db[][][] = mysql.getCombinationAll(N, c, combination_number);
		//System.out.println("DB重複組合せ(制約付き):" +Arrays.deepToString(value_db[0]));
		//int value_dbeach[][] = mysql.getCombinationEachn(roop, c, n_combination_number);
		//System.out.println("DB重複組合せ(制約付き):" +Arrays.deepToString(value_dbeach));
		
		//(4)理論値解の算出
		//getMVAの中で、重複組み合わせを取得する
		MVA_lib mlib = new MVA_lib(c, K, N, nc, mu,alpha2);
		mlib.getMVA(combination_id, transition_id);
		double L[][][] = mlib.getL();
		double W[][][][] = mlib.getW();
		double lambda[][][] = mlib.getLambda();
		double L_node[] = new double[K];
		System.out.println("W:" +Arrays.deepToString(W));
		System.out.println("λ:" +Arrays.deepToString(lambda));
		System.out.println("L:" +Arrays.deepToString(L));
		
		for(int i = 0; i < W.length; i++) {
			for(int j = 0; j < W[i].length; j++) {
				System.out.println("W["+i+"]["+j+"]["+nc[0]+"]["+nc[1]+"]= "+W[i][j][nc[0]][nc[1]]);
			}
		}
		for(int i = 0; i < lambda.length; i++) {
			System.out.println("lambda["+i+"]["+nc[0]+"]["+nc[1]+"]= "+lambda[i][nc[0]][nc[1]]);
		}
		for(int i = 0; i < L.length; i++) {
			System.out.println("L["+i+"]["+nc[0]+"]["+nc[1]+"]= "+L[i][nc[0]][nc[1]]);
			L_node[i] = L[i][nc[0]][nc[1]];
		}
		System.out.println("L_node:" +Arrays.toString(L_node));
		mysql.insertL(L_node);
		
		//(5)Simulation
		int time = 300000;
		BCMP_Simulation slib = new BCMP_Simulation(r, mu_sim, time, node_index, K, N, c);
		System.out.println("Simulation Start");
		double [][] result = slib.getSimulation();
		System.out.println("Result:" +Arrays.deepToString(result));
		//DB格納形式に変更
		double [][]simulation_L = new double[c][K];
		for(int i = 0; i < c; i++) {
			for(int j = 0; j < K; j ++) {
				simulation_L[i][j] = result[0][i * K + j];
			}
		}
		mysql.insertSimulationL(simulation_L, time);
	}

	public void getCSV2(String path, int K, int c, double r[][]) {
		//CSVから取り込み
		try {
			File f = new File(path);
			BufferedReader br = new BufferedReader(new FileReader(f));
				 
			String[][] data = new String[K * c][K * c]; 
			String line = br.readLine();
			for (int i = 0; line != null; i++) {
				data[i] = line.split(",", 0);
				line = br.readLine();
			}
			br.close();
			// CSVから読み込んだ配列の中身を処理
			for(int i = 0; i < data.length; i++) {
				for(int j = 0; j < data[0].length; j++) {
					r[i][j] = Double.parseDouble(data[i][j]);
				}
			} 
		} catch (IOException e) {
			System.out.println(e);
		}
		//CSVから取り込みここまで	
	}
}
