package bcmp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL {
	String driver;// JDBCドライバの登録
    String server, dbname, url, user, password;// データベースの指定
    Connection con;
    Statement stmt;
    ResultSet rs;
    int combination_id, transition_id;
    
	public MySQL(int combination_id, int transition_id) {
		this.driver = "org.gjt.mm.mysql.Driver";
		this.server = "mznbcmp.mizunolab.info";
		this.dbname = "mznbcmp";
		this.url = "jdbc:mysql://" + server + "/" + dbname + "?useUnicode=true&characterEncoding=UTF-8";
		this.user = "mznbcmp";
		this.password = "kansou";
		try {
			this.con = DriverManager.getConnection(url, user, password);
			this.stmt = con.createStatement ();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.combination_id = combination_id;
		this.transition_id = transition_id;
	}
	
	//課題
	//本当は一度に組み合わせを全部持っていきたいけど、３次元配列は面倒なので、一回ずつ持ってくる
	//処理が重かったら、全部持ってくる方法にする
	//全ての件数はレコード数/cなので、cの数も必要(DBから、プログラムからどちらでも)
	//この情報を使って動くようにする
	public int getCombinations() { //総組み合わせ数を返す(cHN：全体数)
		int combination_number = -1;
		String sqlcount = "select combination_num FROM combinations";
		ResultSet rscount;
		try {
			rscount = stmt.executeQuery(sqlcount);
			rscount.next();
			combination_number = rscount.getInt("combination_num");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return combination_number;
	}

	public int getCombinations(int n,int C) { //ループ中のnを利用して総組み合わせ数を返す(cHn：全体数)
		int combination_number = -1;
		String sqlcount = "select count(combination_id = "+combination_id+" and n = "+n+" OR NULL) cnt FROM combination_values";
		ResultSet rscount;
		try {
			rscount = stmt.executeQuery(sqlcount);
			rscount.next();
			combination_number = rscount.getInt("cnt") / C;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return combination_number;
	}

	//これだと、配列数がnで違ってしまってうまくいかない
	public int[][][] getCombinationAll(int N, int C, int total){
		int [][][]value = new int[N][total][C];
		String sqlselect = "select * FROM combination_values where combination_id = "+combination_id;
		ResultSet rs;
		try {
			rs = stmt.executeQuery(sqlselect);
			int idx = 0;
			int flg = 1;
			while(rs.next()) {
				int n_index = rs.getInt("n");//nは1からなので注意
				int c_index = rs.getInt("index");
				int c_value = rs.getInt("value");
				System.out.println("n_index ="+n_index+",idx ="+idx+",c_index ="+c_index+"c_value ="+c_value);
				value[n_index-1][idx][c_index] = c_value;
				if(flg % C != 0)flg ++; //idxをあげるのはクラス数入れ終わってから
				else if(flg % C == 0) {
					idx ++;
					flg = 1;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}
	
	public int[][] getCombinationEachn(int n, int C, int total){
		int [][]value = new int[total][C];
		String sqlselect = "select * FROM combination_values where combination_id = "+combination_id+" and n ="+n ;
		ResultSet rs;
		try {
			rs = stmt.executeQuery(sqlselect);
			int idx = 0;
			int flg = 1;
			while(rs.next()) {
				int c_index = rs.getInt("index");
				int c_value = rs.getInt("value");
				//System.out.println("idx ="+idx+",c_index ="+c_index+"c_value ="+c_value);
				value[idx][c_index] = c_value;
				if(flg % C != 0)flg ++; //idxをあげるのはクラス数入れ終わってから
				else if(flg % C == 0) {
					idx ++;
					flg = 1;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return value;
	}
	
	public void insertL(double L[]){
		System.out.println("MVA平均系内人数L : Insert開始");
        try{
        	StringBuffer buf = new StringBuffer();
			buf.append("INSERT INTO results(combination_id, transition_id, node_id, L) VALUES");
        	for(int i = 0; i < L.length; i++){
        		if(i == L.length -1)
                	buf.append("("+combination_id+","+transition_id+","+i+","+L[i]+")");
                else buf.append("("+combination_id+","+transition_id+","+i+","+L[i]+"),");
            }
            String sql = buf.toString();
            stmt.execute (sql);
        }
        catch (SQLException e) {
                e.printStackTrace();
        }
        System.out.println("MVA平均系内人数L : Insert完了");
	}
	
	public void insertSimulationL(double L[][], int time){//L[C][K]
		System.out.println("Simulation平均系内人数L : Insert開始");
        try{
        	StringBuffer buf = new StringBuffer();
			buf.append("INSERT INTO simulations(combination_id, transition_id, class_id, node_id, L,time) VALUES");
        	for(int i = 0; i < L.length; i++){
        		for(int j = 0; j < L[0].length; j++) {
        			if(i == L.length -1 && j == L[0].length -1) 
        				buf.append("("+combination_id+","+transition_id+","+i+","+j+","+L[i][j]+","+time+")");
        			else buf.append("("+combination_id+","+transition_id+","+i+","+j+","+L[i][j]+","+time+"),");
        		}
            }
            String sql = buf.toString();
            stmt.execute (sql);
        }
        catch (SQLException e) {
                e.printStackTrace();
        }
        System.out.println("Simualtion平均系内人数L : Insert完了");
	}
}
