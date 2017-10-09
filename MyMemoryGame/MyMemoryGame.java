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
	private ButtonHandler bh = new ButtonHandler();//�\����ƥ�
	private JMenuBar jmb;
	private int ROW = 4;
	private int COL = 4;
	private float GAMETIME = 0;
	private static DecimalFormat df = new DecimalFormat("0.0");
	private final int LOOK_SEC = 4; //�]�w�@�}�l�i�ݴX��
	private final int ADD = 1000, DECR = 100;//�o��1000�A����100
	private final int SEC = 1; //�]�w½�}���P���ۦP�ɶ��j��Ʒ|�\�^�h
	private int imgH = 0, imgW = 0, score = 0; //imgH�ϰ��AimgW�ϼe�Ascore����
	private JMenu game = new JMenu("�C��");
	private JMenu music = new JMenu("����");
	private JMenu about = new JMenu("����");
	private JMenuItem[] gm = new JMenuItem[5];//�C��
	private JMenuItem[] abo = new JMenuItem[2];//����
	private JMenuItem[] gmsize = new JMenuItem[4];//�C���j�p
	private JMenuItem[] gmpic = new JMenuItem[2];//�C���Ϥ�
	private JMenuItem[] bgm = new JMenuItem[6];//����
	private JRadioButtonMenuItem[] gmeasy = new JRadioButtonMenuItem[2];//������
	private ButtonGroup btngroup = new ButtonGroup();
	private ButtonGroup btngroup1 = new ButtonGroup();
	private JButton[] imgBtn = new JButton[ROW * COL]; //���s
	private ImageIcon[] img = new ImageIcon[ROW * COL]; //�s��Ϫ���
	private JPanel jpl = new JPanel(new GridLayout(ROW, COL, 0, 0));
	private JPanel mypanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 13));
	private JLabel jl = new JLabel("SCORE : 0");
	private JLabel gmtimel = new JLabel("�C���ɶ� : ");
	private Timer gmTimer = new Timer();
	private	Clip gmclip;
	private	URL gmurl;
	private AudioInputStream gmMusic;
	private int[] m;//�s��C�����s�����Ϥ�����
	private int[] twoImg = { -1, -1 }; //�O���ثe½�}����i�Ͻs��
	private int[] btnIndex = { -1, -1 }; //�O���Q½�}��i�Ϫ����s��m
	private boolean isEasy = true;
	private boolean isChange = false; //�ΨӤ����s���i�϶���
	private boolean isStart = false; //�O�_�}�l
	private boolean isStop = false; //�O�_�Ȱ�
	private boolean[] btnDown = new boolean[ROW*COL]; //�O���Q���L�����s
	private Set<Integer> btnSet = new HashSet<>(); //�O���w�t�令�\�L�����s

	public MyMemoryGame()
	{
		super("�O�Фj�D��");
		Container c = getContentPane();
		c = this.getContentPane();
		jmb = new JMenuBar();
		this.setJMenuBar(jmb); //�[�J�u��C

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
		c.add(jl, BorderLayout.SOUTH); //�N����label���̤U��
		mypanel.add(gmtimel);
		mypanel.setLayout(new FlowLayout(FlowLayout.LEFT, 7, 13));
		mypanel.setPreferredSize(new Dimension(130, 0));
		c.add(mypanel, BorderLayout.EAST);
		//�C������ܶ���
		jmb.add(game);
		gm[0] = new JMenu("�}�l�C��");
		gm[1] = new JMenu("������");
		gm[2] = new JMenuItem("���}�C��");
		gm[3] = new JMenuItem("�C���Ȱ�/�~��");
		gm[4] = new JMenu("�C���Ϥ�");
		game.add(gm[0]);
		game.add(gm[1]);
		game.add(gm[3]);
		game.add(gm[4]);
		gm[3].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		game.addSeparator();
		game.add(gm[2]);

		//�C���j�p����ܶ���
		gmsize[0] = new JMenuItem("4x3");
		gmsize[1] = new JMenuItem("4x4");
		gmsize[2] = new JMenuItem("5x4");
		gmsize[3] = new JMenuItem("���W�E��");
		gmsize[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		gmsize[1].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		gmsize[2].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
		gmsize[3].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
		gm[0].add(gmsize[0]);
		gm[0].add(gmsize[1]);
		gm[0].add(gmsize[2]);
		gm[0].add(gmsize[3]);

		//�����ת���ܶ���
		gmeasy[0] = new JRadioButtonMenuItem("²��");
		gmeasy[1] = new JRadioButtonMenuItem("�x��");
		btngroup.add(gmeasy[0]);
		btngroup.add(gmeasy[1]);
		gmeasy[0].setSelected(true);
		gm[1].add(gmeasy[0]);
		gm[1].add(gmeasy[1]);

		//�C���Ϥ�����ܶ���
		gmpic[0] = new JRadioButtonMenuItem("����߿�");
		gmpic[1] = new JRadioButtonMenuItem("�¤H�ݸ�");
		btngroup1.add(gmpic[0]);
		btngroup1.add(gmpic[1]);
		gmpic[0].setSelected(true);
		gm[4].add(gmpic[0]);
		gm[4].add(gmpic[1]);

		//���֪���ܶ���
		jmb.add(music);
		bgm[0] = new JMenuItem("Chicken Attack");
		bgm[1] = new JMenuItem("HEYEAYEA");
		bgm[2] = new JMenuItem("Megu Megu Fire");
		bgm[3] = new JMenuItem("Shooting Star");
		bgm[4] = new JMenuItem("Tokyo Hot");
		bgm[5] = new JMenuItem("�U��������");
		for (int m = 0; m<bgm.length; m++)
			music.add(bgm[m]);

		//���󪺿�ܶ���
		jmb.add(about);
		abo[0] = new JMenuItem("�C������");
		abo[1] = new JMenuItem("�@��");
		about.add(abo[0]);
		about.add(abo[1]);
		abo[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));

		//�]�w����
		imgH = img[0].getIconHeight() * ROW; //���o�Ϥ�����
		imgW = img[0].getIconWidth() * COL;  //���o�Ϥ��e��
		setSize(imgW + 130, imgH);
		setLocation(200, 100);
		setResizable(false);//������j���s�L�� 
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//���U�\�����ť��
		for (int m = 0; m<gm.length; m++)
			gm[m].addActionListener(bh);
		for (int m = 0; m<gmsize.length; m++)
			gmsize[m].addActionListener(bh);
		for (int m = 0; m<bgm.length; m++)
			bgm[m].addActionListener(bh);
		for (int m = 0; m<abo.length; m++)
			abo[m].addActionListener(bh);
	}
	//�\����ƥ�B�z
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
					gmurl = getClass().getClassLoader().getResource("music/�U��������.wav");
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
			//���s�ƥ�
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
			else if (ae.getSource() == gm[2]) //�����C��
			{
				System.exit(0);
			}
			else if (ae.getSource() == gm[3]) //�C���Ȱ�or�~��
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
							gmtimel.setText("�C���ɶ� : " + df.format(GAMETIME));
						} };
					gmTimer.schedule(task, 0, 100);
					for (int i = 0; i < ROW*COL; i++)
					{
						imgBtn[i].setEnabled(true);
					}
					isStop = false;
				}
			}
			else if (ae.getSource() == abo[0]) //����
			{
				JOptionPane.showMessageDialog(null,
					"1.�C���}�l�e�i��ܡG\n" +
					"    (1) �Ϥ��˦�\n" +
					"    (2) �Ϥ��ƶq(4x3,4x4,5x4)\n" +
					"    (3) ������\n" +
					"2.�C���}�l�ɷ|��" + LOOK_SEC + "���ɶ��Ѫ��a�O�йϤ�\n" +
					"3.½���Ϥ��ɡA���~�Ϥ��|���d" + SEC + "��\n" +
					"4.�q��o" + ADD + "���A�q����" + DECR + "\n" +
					"5.������ܦb���U��A�k��h��ܹC���ɶ�\n" +
					"6.�i��ܭI������\n" +
					"----------------------------------------------\n" +
					"�i�ϥΧֱ���G\n" +
					"F2=�H4x3�}�l�C��\n" +
					"F3=�H4x4�}�l�C��\n" +
					"F4=�H5x4�}�l�C��\n" +
					"F5=�C���Ȱ�/�~��\n",
					"�C�������p�U�G",
					JOptionPane.INFORMATION_MESSAGE);
			}
			else if (ae.getSource() == abo[1]) //�@��
			{
				JOptionPane.showMessageDialog(null,
					"        �{���]�p�G�L����\n" +
					"                  �B�x��\n" +
					"                  �L�a��",
					"�@��",
					JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	//�^�Ǥ@�Ӧs��1~N�ƭȶüư}�C
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

	//�N�O�����Ϥ����ޭ��ഫ��1~(row*col/2)
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
				gmtimel.setText("�C���ɶ� : " + df.format(GAMETIME));
			} };
		gmTimer.schedule(task, 0, 100);

		reWinSize();
		imgBtn = new JButton[ROW * COL];
		img = new ImageIcon[ROW * COL];
		btnDown = new boolean[ROW*COL];
		btnSet.clear(); //�N�O���L�B�w�t�諸���s�M��
		jpl.removeAll();
		jpl.setLayout(new GridLayout(ROW, COL, 0, 0));

		//��l�ƩҦ����s�W���ϡA�w�]�����i�H��
		for (int i = 0; i < ROW*COL; i++)
		{
			img[i] = new ImageIcon(getClass().getResource("images/p0.jpg"));
			imgBtn[i] = new JButton(img[i]);
			imgBtn[i].addActionListener(bh);
			imgBtn[i].setEnabled(false);
			jpl.add(imgBtn[i]);
		}
		//�]�w����
		imgH = img[0].getIconHeight() * ROW; //���o�Ϥ�����
		imgW = img[0].getIconWidth() * COL;  //���o�Ϥ��e��
		setSize(imgW + 130 + 25, imgH + 25);

	}

	//�}�l(����)�C��
	private void replay()
	{
		reset();
		m = rand(ROW*COL); //���o���s�U�����ƪ��Ϯ׶���

		//�]�w�Ҧ��Ϥ����i�H½���A
		for (int i = 0; i < ROW*COL; i++)
		{
			imgBtn[i].setEnabled(true);
			if (gmpic[0].isSelected())
				imgBtn[i].setIcon(new ImageIcon(getClass().getResource("images/p" + imgIndex(m[i]) + ".jpg")));
			else
				imgBtn[i].setIcon(new ImageIcon(getClass().getResource("images0/p" + imgIndex(m[i]) + ".jpg")));
		}

		//�M���O����i½�}�Ϥ����s�P��m
		twoImg[0] = twoImg[1] = btnIndex[0] = btnIndex[1] = -1;
		isChange = false; //���]½�P����
		score = 0; //�����k�s
		jl.setText("SCORE : " + score); //��s��ܤ���
		isStart = false;

		new Thread(new Runnable()
		{
			public void run()
			{
				try{
					Thread.sleep(LOOK_SEC * 1000);
				}
				catch (InterruptedException e){}
				//�N�ϲM�אּ�w�]�ϡA�ó]�w�L�k������
				for (int i = 0; i < ROW*COL; i++)
				{
					img[i] = new ImageIcon(getClass().getResource("images/p0.jpg"));
					imgBtn[i].setIcon(img[i]);
					btnDown[i] = false; //�N�O���L�����s�M��
				}
				isStart = true;
			}
		}).start();

	}

	//�ˬd�O�ثe��i�ϬO�_�t��
	private void checkIsRight(int btnN, int jpgNum)
	{
		if (!isStart) return; //�P�_�O�_�w�����a�ݧ��Ϥ��ƦC�A�A�}�l
		if (btnDown[btnN]) return;//�p�G���U���Ϥ��w½�}�A�N���}method
		btnDown[btnN] = true; //��ܦ����s�w½�}���A

		if (gmpic[0].isSelected())
			imgBtn[btnN].setIcon(new ImageIcon(getClass().getResource("images/p" + jpgNum + ".jpg")));
		else
			imgBtn[btnN].setIcon(new ImageIcon(getClass().getResource("images0/p" + jpgNum + ".jpg")));

		//�O���W�i�P�ثe�Ϥ��s���B���s��mtwoImg[1]�O�W�@�i�AtwoImg[0]�O�ثe      		        	
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
				score += ADD; //�t�令�\���ƼW�[
				jl.setText("SCORE : " + score);
				checkIsGameOver();
				return;
			}

			//�]�w½�}��i�Ϥ��ɡA��L�Ϥ�����½
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

		//�\��쥻½�}���Ϥ�        	
		btnDown[btnIndex[0]] = false;
		btnDown[btnIndex[1]] = false;
		imgBtn[btnIndex[0]].setIcon(new ImageIcon(getClass().getResource("images/p0.jpg")));
		imgBtn[btnIndex[1]].setIcon(new ImageIcon(getClass().getResource("images/p0.jpg")));

		//�]�w�\�^��i½�}�Ϥ��ɡA��L�Ϥ��i�H�^�Хi�H½�����A
		for (int i = 0; i < ROW*COL; i++)
		if (!btnDown[i]) imgBtn[i].setEnabled(true);

		score -= DECR; //�q������
		if (score<0)score = 0;
		jl.setText("SCORE : " + score);
	}

	public static void main(String[] args)
	{
		new MyMemoryGame();
	}
}