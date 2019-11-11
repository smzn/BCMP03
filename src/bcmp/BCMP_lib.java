package bcmp;

public class BCMP_lib {

	private int K,c,NK,N;
	double r1[][], b1[], alpha[], alpha_class[][];//alphaは１次元、alpha_classはクラス別の2次元
	
	public BCMP_lib(int k, int c, int n, double mu[][]) {
		K = k; //ノード数
		this.c = c; //クラス数
		this.N = n; //系内人数
		NK = K * c -1;
		r1 = new double[K * c -1][K * c -1];//Rを転置して対角要素を-1、１行と１列要素を削除
		b1 = new double[K * c -1]; //Rの１行目の2列目要素からK*c要素まで
		alpha = new double[K * c];
		alpha_class = new double[c][K];
	}

	public double [] calcGauss(double[][] a, double[] b){
		int p;
		double pmax, s;
		double w[] = new double[NK];
		/* 前進消去（ピボット選択）*/
		for(int k = 0; k < NK-1; k++){  /* 第ｋステップ */
		      p = k;
		      pmax = Math.abs( a[k][k] );
		      for(int i = k+1; i < NK; i++){  /* ピボット選択 */
		         if(Math.abs( a[i][k] ) > pmax){
		            p = i;
		            pmax = Math.abs( a[i][k] );
		         }
		      }

		      if(p != k){  /* 第ｋ行と第ｐ行の交換　*/
		         for(int i = k; i < NK; i++){
		            /* 係数行列　*/
		            s = a[k][i];
		            a[k][i] = a[p][i];
		            a[p][i] = s;
		         }
		         /* 既知ベクトル */
		         s = b[k];
		         b[k] = b[p];
		         b[p] = s;
		      }
		/* 前進消去 */
		      for(int i = k +1; i < NK; i++){ /* 第ｉ行 */
		         w[i] = a[i][k] / a[k][k];
		         a[i][k] = 0.0;
		         /* 第ｋ行を-a[i][k]/a[k][k]倍して、第ｉ行に加える */
		         for(int j = k + 1; j < NK; j++){
		            a[i][j] = a[i][j] - a[k][j] * w[i];
		         }
		         b[i] = b[i] - b[k] * w[i];
		      }
		   }
		/* 後退代入 */
		      for(int i = NK - 1; i >= 0; i--){
		         for(int j = i + 1; j < NK; j++){
		            b[i] = b[i] - a[i][j] * b[j];
		            a[i][j] = 0.0;
		         }
		         b[i] = b[i] / a[i][i];
		         a[i][i] = 1.0;
		      }
		
		return b;
	}
	
	//トラフィック方程式を解く準備
	public void getPretrafiic(double [][]r) { 
		for(int i = 0; i < r.length -1; i++){
			for(int j = 0; j < r.length -1; j++){
				if( i == j ) {
					r1[i][j] = r[j + 1][i + 1] - 1;//転置して、対角要素は-1(１行、一列の各要素は除く) 
				}else {
					r1[i][j] = r[j + 1][i + 1];
				}
			}
		}
		for(int i = 0;i < r.length -1; i++){
			b1[i] = -r[0][i+1];
		}
	}

	public double[][] getR1() {
		return r1;
	}

	public double[] getB1() {
		return b1;
	}
	
	//alpha1にα11を追加してalphaとする
	public void getAlpha_value(double alpha1[]){
		//alpha作成
		for(int i = 0 ; i < alpha.length; i++){
			if( i == 0) alpha[i] = 1;
			else alpha[i] = alpha1[i-1];
		}
		//alpha_class(クラス別２次元に直す)
		for(int i = 0; i < c; i++) {
			for(int j = 0; j < K; j++) {
				alpha_class[i][j] = alpha[i * K + j];
			}
		}
	}

	public double[] getAlpha() {
		return alpha;
	}

	public double[][] getAlpha_class() {
		return alpha_class;
	}
	
}
