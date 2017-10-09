import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;
import java.text.DecimalFormat;
import java.applet.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.net.*;
import sun.audio.*;
import javax.sound.sampled.*;
import javax.sound.sampled.Clip;

public class MyMemoryGame extends JFrame implements Runnable
{
	private ButtonHandler bh = new ButtonHandler();//功能表單事件
	private JMenuBar jmb;
	private int ROW = 4;
	private int COL = 4;
	private float GAMETIME = 0;
	private static DecimalFormat df = new DecimalFormat("0.0");
	private final int LOOK_SEC = 4; //設定一開始可看幾秒
	private final int ADD = 1000, DECR = 100;//得分1000，扣分100
	private final int SEC = 1; //設定翻開的牌不相同時間隔秒數會蓋回去
	private int imgH = 0, imgW = 0, score = 0; //imgH圖高，imgW圖寬，score分數
	private JMenu game = new JMenu("遊戲");
	private JMenu music = new JMenu("音樂");
	private JMenu about = new JMenu("關於");
	private JMenuItem[] gm = new JMenuItem[5];//遊戲
	private JMenuItem[] abo = new JMenuItem[2];//關於
	private JMenuItem[] gmsize = new JMenuItem[4];//遊戲大小
	private JMenuItem[] gmpic = new JMenuItem[2];//遊戲圖片
	private JMenuItem[] bgm = new JMenuItem[6];//音樂
	private JRadioButtonMenuItem[] gmeasy = new JRadioButtonMenuItem[2];//難易度
	private ButtonGroup btngroup = new ButtonGroup();
	private ButtonGroup btngroup1 = new ButtonGroup();
	private JButton[] imgBtn = new JButton[ROW * COL]; //按鈕
	private ImageIcon[] img = new ImageIcon[ROW * COL]; //存放圖物件
	private JPanel jpl = new JPanel(new GridLayout(ROW, COL, 0, 0));
	private JPanel mypanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 13));
	private JLabel jl = new JLabel("SCORE : 0");
	private JLabel gmtimel = new JLabel("遊戲時間 : ");
	private Timer gmTimer = new Timer();
	private	Clip gmclip;
	private	URL gmurl;
	private AudioInputStream gmMusic;
	private int[] m;//存放每次重新玩的圖片順序
	private int[] twoImg = { -1, -1 }; //記錄目前翻開的兩張圖編號
	private int[] btnIndex = { -1, -1 }; //記錄被翻開兩張圖的按鈕位置
	private boolean isEasy = true;
	private boolean isChange = false; //用來切換存放兩張圖順序
	private boolean isStart = false; //是否開始
	private boolean isStop = false; //是否暫停
	private boolean[] btnDown = new boolean[ROW*COL]; //記錄被按過的按鈕
	private Set<Integer> btnSet = new HashSet<>(); //記錄已配對成功過的按鈕

	public MyMemoryGame()
	{
		super("記憶大挑戰");
		Container c = getContentPane();
		c = this.getContentPane();
		jmb = new JMenuBar();
		this.setJMenuBar(jmb); //加入工具列

		for (int i = 0; i < ROW*COL; i++)
		{
			img[i] = new ImageIcon(getClass().getResource("images/p0.jpg"));
			imgBtn[i] = new JButton(img[i]);
			imgBtn[i].addActionListener(bh);
			imgBtn[i].setEnabled(false);
			jpl.add(imgBtn[i]);
		}
		jpl.add(jl);
		c.add(jpl, BorderLayout.CENTER);
		c.add(jl, BorderLayout.SOUTH); //將分數label放到最下面
		mypanel.add(gmtimel);
		mypanel.setLayout(new FlowLayout(FlowLayout.LEFT, 7, 13));
		mypanel.setPreferredSize(new Dimension(130, 0));
		c.add(mypanel, BorderLayout.EAST);
		//遊戲的選擇項目
		jmb.add(game);
		gm[0] = new JMenu("開始遊戲");
		gm[1] = new JMenu("難易度");
		gm[2] = new JMenuItem("離開遊戲");
		gm[3] = new JMenuItem("遊戲暫停/繼續");
		gm[4] = new JMenu("遊戲圖片");
		game.add(gm[0]);
		game.add(gm[1]);
		game.add(gm[3]);
		game.add(gm[4]);
		gm[3].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		game.addSeparator();
		game.add(gm[2]);

		//遊戲大小的選擇項目
		gmsize[0] = new JMenuItem("4x3");
		gmsize[1] = new JMenuItem("4x4");
		gmsize[2] = new JMenuItem("5x4");
		gmsize[3] = new JMenuItem("＊超激＊");
		gmsize[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		gmsize[1].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		gmsize[2].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
		gmsize[3].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
		gm[0].add(gmsize[0]);
		gm[0].add(gmsize[1]);
		gm[0].add(gmsize[2]);
		gm[0].add(gmsize[3]);

		//難易度的選擇項目
		gmeasy[0] = new JRadioButtonMenuItem("簡單");
		gmeasy[1] = new JRadioButtonMenuItem("困難");
		btngroup.add(gmeasy[0]);
		btngroup.add(gmeasy[1]);
		gmeasy[0].setSelected(true);
		gm[1].add(gmeasy[0]);
		gm[1].add(gmeasy[1]);

		//遊戲圖片的選擇項目
		gmpic[0] = new JRadioButtonMenuItem("白爛貓貓");
		gmpic[1] = new JRadioButtonMenuItem("黑人問號");
		btngroup1.add(gmpic[0]);
		btngroup1.add(gmpic[1]);
		gmpic[0].setSelected(true);
		gm[4].add(gmpic[0]);
		gm[4].add(gmpic[1]);

		//音樂的選擇項目
		jmb.add(music);
		bgm[0] = new JMenuItem("Chicken Attack");
		bgm[1] = new JMenuItem("HEYEAYEA");
		bgm[2] = new JMenuItem("Megu Megu Fire");
		bgm[3] = new JMenuItem("Shooting Star");
		bgm[4] = new JMenuItem("Tokyo Hot");
		bgm[5] = new JMenuItem("萬里的長城");
		for (int m = 0; m<bgm.length; m++)
			music.add(bgm[m]);

		//關於的選擇項目
		jmb.add(about);
		abo[0] = new JMenuItem("遊戲說明");
		abo[1] = new JMenuItem("作者");
		about.add(abo[0]);
		about.add(abo[1]);
		abo[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

		//設定視窗
		imgH = img[0].getIconHeight() * ROW; //取得圖片高度
		imgW = img[0].getIconWidth() * COL;  //取得圖片寬度
		setSize(imgW + 130, imgH);
		setLocation(200, 100);
		setResizable(false);//視窗放大按鈕無效 
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//註冊功能表單傾聽者
		for (int m = 0; m<gm.length; m++)
			gm[m].addActionListener(bh);
		for (int m = 0; m<gmsize.length; m++)
			gmsize[m].addActionListener(bh);
		for (int m = 0; m<bgm.length; m++)
			bgm[m].addActionListener(bh);
		for (int m = 0; m<abo.length; m++)
			abo[m].addActionListener(bh);
	}
	//功能表單事件處理
	private class ButtonHandler implements  ActionListener
	{

		public void actionPerformed(ActionEvent ae)
		{
			if (ae.getSource() == bgm[0])
			{
				try
				{
					if (gmclip != null)
						gmclip.stop();
					gmurl = getClass().getClassLoader().getResource("music/Chicken Attack.wav");
					gmMusic = AudioSystem.getAudioInputStream(gmurl);
					gmclip = AudioSystem.getClip();
					gmclip.open(gmMusic);
					gmclip.start();
					gmclip.loop(Clip.LOOP_CONTINUOUSLY);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			else if (ae.getSource() == bgm[1])
			{
				try
				{
					if (gmclip != null)
						gmclip.stop();
					gmurl = getClass().getClassLoader().getResource("music/HEYEAYEA.wav");
					gmMusic = AudioSystem.getAudioInputStream(gmurl);
					gmclip = AudioSystem.getClip();
					gmclip.open(gmMusic);
					gmclip.start();
					gmclip.loop(Clip.LOOP_CONTINUOUSLY);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			else if (ae.getSource() == bgm[2])
			{
				try
				{
					if (gmclip != null)
						gmclip.stop();
					gmurl = getClass().getClassLoader().getResource("music/Megu Megu Fire.wav");
					gmMusic = AudioSystem.getAudioInputStream(gmurl);
					gmclip = AudioSystem.getClip();
					gmclip.open(gmMusic);
					gmclip.start();
					gmclip.loop(Clip.LOOP_CONTINUOUSLY);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			else if (ae.getSource() == bgm[3])
			{
				try
				{
					if (gmclip != null)
						gmclip.stop();
					gmurl = getClass().getClassLoader().getResource("music/Shooting Star.wav");
					gmMusic = AudioSystem.getAudioInputStream(gmurl);
					gmclip = AudioSystem.getClip();
					gmclip.open(gmMusic);
					gmclip.start();
					gmclip.loop(Clip.LOOP_CONTINUOUSLY);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			else if (ae.getSource() == bgm[4])
			{
				try
				{
					if (gmclip != null)
						gmclip.stop();
					gmurl = getClass().getClassLoader().getResource("music/Tokyo Hot.wav");
					gmMusic = AudioSystem.getAudioInputStream(gmurl);
					gmclip = AudioSystem.getClip();
					gmclip.open(gmMusic);
					gmclip.start();
					gmclip.loop(Clip.LOOP_CONTINUOUSLY);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			else if (ae.getSource() == bgm[5])
			{
				try
				{
					if (gmclip != null)
						gmclip.stop();
					gmurl = getClass().getClassLoader().getResource("music/萬里的長城.wav");
					gmMusic = AudioSystem.getAudioInputStream(gmurl);
					gmclip = AudioSystem.getClip();
					gmclip.open(gmMusic);
					gmclip.start();
					gmclip.loop(Clip.LOOP_CONTINUOUSLY);
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			//按鈕事件
			for (int i = 0; i < ROW*COL; i++)
			{
				if (ae.getSource() == imgBtn[i])
				{
					checkIsRight(i, imgIndex(m[i]));
					break;
				}
			}
			if (ae.getSource() == gmsize[0]) //4x3
			{
				ROW = 4;
				COL = 3;
				replay();
			}
			else if (ae.getSource() == gmsize[1]) //4x4
			{
				ROW = 4;
				COL = 4;
				replay();
			}
			else if (ae.getSource() == gmsize[2]) //5x4
			{
				ROW = 5;
				COL = 4;
				replay();
			}
			else if (ae.getSource() == gmsize[3]) //8x5
			{
				ROW = 5;
				COL = 8;
				replay();
			}
			else if (ae.getSource() == gm[2]) //結束遊戲
			{
				System.exit(0);
			}
			else if (ae.getSource() == gm[3]) //遊戲暫停or繼續
			{
				if (!isStop)
				{
					gmTimer.cancel();
					for (int i = 0; i < ROW*COL; i++)
					{
						imgBtn[i].setEnabled(false);
					}
					isStop = true;
				}
				else
				{
					gmTimer = new Timer();
					TimerTask task = new TimerTask(){
						public void run(){
							GAMETIME += 0.1;
							gmtimel.setText("遊戲時間 : " + df.format(GAMETIME));
						} };
					gmTimer.schedule(task, 0, 100);
					for (int i = 0; i < ROW*COL; i++)
					{
						imgBtn[i].setEnabled(true);
					}
					isStop = false;
				}
			}
			else if (ae.getSource() == abo[0]) //關於
			{
				JOptionPane.showMessageDialog(null,
					"1.遊戲開始前可選擇：\n" +
					"    (1) 圖片樣式\n" +
					"    (2) 圖片數量(4x3,4x4,5x4)\n" +
					"    (3) 難易度\n" +
					"2.遊戲開始時會有" + LOOK_SEC + "秒的時間供玩家記憶圖片\n" +
					"3.翻錯圖片時，錯誤圖片會停留" + SEC + "秒\n" +
					"4.猜對得" + ADD + "分，猜錯扣" + DECR + "\n" +
					"5.分數顯示在左下方，右方則顯示遊戲時間\n" +
					"6.可選擇背景音樂\n" +
					"----------------------------------------------\n" +
					"可使用快捷鍵：\n" +
					"F2=以4x3開始遊戲\n" +
					"F3=以4x4開始遊戲\n" +
					"F4=以5x4開始遊戲\n" +
					"F5=遊戲暫停/繼續\n",
					"遊戲說明如下：",
					JOptionPane.INFORMATION_MESSAGE);
			}
			else if (ae.getSource() == abo[1]) //作者
			{
				JOptionPane.showMessageDialog(null,
					"        程式設計：林建豐\n" +
					"                  劉庭佑\n" +
					"                  林軒正",
					"作者",
					JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	//回傳一個存放1~N數值亂數陣列
	private int[] rand(int N)
	{
		int temp[] = new int[N];

		for (int i = 0; i < N; i++)
		{
			temp[i] = i;
		}
		for (int i = 0; i < N; i++)
		{
			int j = (int)(Math.random() * N);
			int tmp = temp[i];
			temp[i] = temp[j];
			temp[j] = tmp;
		}
		return temp;
	}

	//將記錄的圖片索引值轉換為1~(row*col/2)
	private int imgIndex(int n)
	{
		int tmp;
		if (gmeasy[0].isSelected())
			tmp = n / 4 + 1;
		else
			tmp = n / 2 + 1;
		return tmp;
	}

	public void reWinSize()
	{
		imgH = img[0].getIconHeight() * ROW;
		imgW = img[0].getIconWidth() * COL;
		setSize(imgW + 129 + 25, imgH + 25);
	}

	public void reset()
	{
		isStop = false;
		GAMETIME = 0;
		gmTimer.cancel();
		gmTimer = new Timer();
		TimerTask task = new TimerTask(){
			public void run(){
				GAMETIME += 0.1;
				gmtimel.setText("遊戲時間 : " + df.format(GAMETIME));
			} };
		gmTimer.schedule(task, 0, 100);

		reWinSize();
		imgBtn = new JButton[ROW * COL];
		img = new ImageIcon[ROW * COL];
		btnDown = new boolean[ROW*COL];
		btnSet.clear(); //將記錄過且已配對的按鈕清除
		jpl.removeAll();
		jpl.setLayout(new GridLayout(ROW, COL, 0, 0));

		//初始化所有按鈕上的圖，預設為不可以按
		for (int i = 0; i < ROW*COL; i++)
		{
			img[i] = new ImageIcon(getClass().getResource("images/p0.jpg"));
			imgBtn[i] = new JButton(img[i]);
			imgBtn[i].addActionListener(bh);
			imgBtn[i].setEnabled(false);
			jpl.add(imgBtn[i]);
		}
		//設定視窗
		imgH = img[0].getIconHeight() * ROW; //取得圖片高度
		imgW = img[0].getIconWidth() * COL;  //取得圖片寬度
		setSize(imgW + 130 + 25, imgH + 25);

	}

	//開始(重玩)遊戲
	private void replay()
	{
		reset();
		m = rand(ROW*COL); //取得按鈕下次重排的圖案順序

		//設定所有圖片為可以翻狀態
		for (int i = 0; i < ROW*COL; i++)
		{
			imgBtn[i].setEnabled(true);
			if (gmpic[0].isSelected())
				imgBtn[i].setIcon(new ImageIcon(getClass().getResource("images/p" + imgIndex(m[i]) + ".jpg")));
			else
				imgBtn[i].setIcon(new ImageIcon(getClass().getResource("images0/p" + imgIndex(m[i]) + ".jpg")));
		}

		//清除記錄兩張翻開圖片按鈕與位置
		twoImg[0] = twoImg[1] = btnIndex[0] = btnIndex[1] = -1;
		isChange = false; //重設翻牌順序
		score = 0; //分數歸零
		jl.setText("SCORE : " + score); //更新顯示分數
		isStart = false;

		new Thread(new Runnable()
		{
			public void run()
			{
				try{
					Thread.sleep(LOOK_SEC * 1000);
				}
				catch (InterruptedException e){}
				//將圖清改為預設圖，並設定無法按按鍵
				for (int i = 0; i < ROW*COL; i++)
				{
					img[i] = new ImageIcon(getClass().getResource("images/p0.jpg"));
					imgBtn[i].setIcon(img[i]);
					btnDown[i] = false; //將記錄過的按鈕清除
				}
				isStart = true;
			}
		}).start();

	}

	//檢查是目前兩張圖是否配對
	private void checkIsRight(int btnN, int jpgNum)
	{
		if (!isStart) return; //判斷是否已讓玩家看完圖片排列，再開始
		if (btnDown[btnN]) return;//如果按下的圖片已翻開，就離開method
		btnDown[btnN] = true; //表示此按鈕已翻開狀態

		if (gmpic[0].isSelected())
			imgBtn[btnN].setIcon(new ImageIcon(getClass().getResource("images/p" + jpgNum + ".jpg")));
		else
			imgBtn[btnN].setIcon(new ImageIcon(getClass().getResource("images0/p" + jpgNum + ".jpg")));

		//記錄上張與目前圖片編號、按鈕位置twoImg[1]是上一張，twoImg[0]是目前      		        	
		int temp = twoImg[0];
		int btnTemp = btnIndex[0];
		twoImg[0] = jpgNum;
		btnIndex[0] = btnN;
		twoImg[1] = temp;
		btnIndex[1] = btnTemp;

		if (isChange)
		{
			if (twoImg[1] == twoImg[0])
			{
				if (btnSet.contains(btnN))
					return;
				btnSet.add(btnN);
				twoImg[1] = -1;
				btnIndex[1] = -1;
				isChange = false;
				score += ADD; //配對成功分數增加
				jl.setText("SCORE : " + score);
				checkIsGameOver();
				return;
			}

			//設定翻開兩張圖片時，其他圖片不能翻
			for (int i = 0; i < ROW*COL; i++)
			if (!btnDown[i]) imgBtn[i].setEnabled(false);

			new Thread(this).start();
		}
		isChange = !isChange;
	}

	public void checkIsGameOver(){
		if (btnSet.size() == ROW*COL / 2)
		{
			gmTimer.cancel();
			JOptionPane.showMessageDialog(null,
				"Well Done!!", "Mission Completed!!",
				JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void run()
	{
		try{
			Thread.sleep(SEC * 1000);
		}
		catch (InterruptedException e){}

		//蓋住原本翻開的圖片        	
		btnDown[btnIndex[0]] = false;
		btnDown[btnIndex[1]] = false;
		imgBtn[btnIndex[0]].setIcon(new ImageIcon(getClass().getResource("images/p0.jpg")));
		imgBtn[btnIndex[1]].setIcon(new ImageIcon(getClass().getResource("images/p0.jpg")));

		//設定蓋回兩張翻開圖片時，其他圖片可以回覆可以翻的狀態
		for (int i = 0; i < ROW*COL; i++)
		if (!btnDown[i]) imgBtn[i].setEnabled(true);

		score -= DECR; //猜錯扣分
		if (score<0)score = 0;
		jl.setText("SCORE : " + score);
	}

	public static void main(String[] args)
	{
		new MyMemoryGame();
	}
}