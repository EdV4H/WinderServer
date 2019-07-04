package WinderServer.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import WinderServer.main.MainFrame;

class ClientProcThread extends Thread {
    private int number;
    private Socket incoming;
    private InputStreamReader myIsr;
    private BufferedReader myIn;
    public PrintWriter myOut;

    private boolean isConnecting;
    private int id;
    private String name;

    public ClientProcThread(int n, Socket i, InputStreamReader isr, BufferedReader in, PrintWriter out) {
        number = n;
        incoming = i;
        myIsr = isr;
        myIn = in;
        myOut = out;
        isConnecting = true;

        name = "NONAME";
    }

    public void run() {
        try {
            while (isConnecting) {
                String inputLine = myIn.readLine();
                MainFrame.mainController.PrintLog("[ClientProcThread] Received from [" + number + "](ID: " + id + ")\t(msg) " + inputLine);
                if (inputLine != null) {
                    String inputTokens[] = inputLine.split(" ");
                    String cmd = inputTokens[0];
                    switch (cmd) {
                    case "REGISTRATION":
                        String reg_name = inputTokens[1] + " " + inputTokens[2];
                        String reg_birth = inputTokens[3];
                        String reg_comment = inputTokens[4].replace("_", " ");
                        int reg_id = MainFrame.mainController.Registration(reg_name, reg_birth, reg_comment);

                        String reg_msg = "SETID " + String.valueOf(reg_id);
                        myOut.println(reg_msg);
                        break;

                    case "LOGIN":
                        int login_id = Integer.parseInt(inputTokens[1]);
                        try {
                            File file = new File("dat/userlist/" + login_id + ".user");
                            BufferedReader br = new BufferedReader(new FileReader(file));

                            String login_name = br.readLine().replace(" ", "_");
                            String login_birth = br.readLine();
                            String login_comment = br.readLine().replace(" ", "_");

                            br.close();

                            String login_msg = "LOGIN " + login_name + " " + login_birth + " " + login_comment;
                            myOut.println(login_msg);
                            id = login_id;
                            name = login_name;

                        } catch (FileNotFoundException e) {
                            System.out.println(e);
                        } catch (IOException e) {
                            System.out.println(e);
                        }
                        try {
                            File file = new File("dat/reservation/" + login_id + ".resv");
                            BufferedReader br = new BufferedReader(new FileReader(file));

                            String resv_str = br.readLine();
                            while (resv_str != null) {
                                myOut.println(resv_str);
                                myOut.flush();
                                resv_str = br.readLine();
                            }

                            br.close();
                        } catch (FileNotFoundException e) {
                            System.out.println(e);
                        } catch (IOException e) {
                            System.out.println(e);
                        }
                        try {
                            File file = new File("dat/reservation/" + login_id + ".resv");
                            FileWriter fw = new FileWriter(file);
                            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
                        
                            pw.close();
                        } catch (FileNotFoundException e) {
                            System.out.println(e);
                        } catch (IOException e) {
                            System.out.println(e);
                        }
                        break;

                    case "GETCHATLIST":
                        break;

                    case "NEXTINFO":
                        Random rand = new Random();
                        int info_id = rand.nextInt(MainFrame.mainController.getMember()) + 1;
                        while (id == info_id) info_id = rand.nextInt(MainFrame.mainController.getMember() + 1);

                        try {
                            File file = new File("dat/userlist/" + info_id + ".user");
                            BufferedReader br = new BufferedReader(new FileReader(file));

                            String info_name = br.readLine().replace(" ", "_");
                            String info_birth = br.readLine();
                            String info_comment = br.readLine().replace(" ", "_");

                            br.close();

                            String info_msg = "SEARCHINFO " + info_id + " " + info_name + " " + info_birth + " " + info_comment;
                            myOut.println(info_msg);

                        } catch (FileNotFoundException e) {
                            System.out.println(e);
                        } catch (IOException e) {
                            System.out.println(e);
                        }
                        break;

                    case "LIKE":
                        int like_id = Integer.parseInt(inputTokens[1]);
                        MainFrame.mainController.PrintLog("[ServerManager] [ID: " + id + "] like [ID: " + like_id + "]");
                        String like_name = "NONAME";

                        try {
                            File file = new File("dat/userlist/" + like_id + ".user");
                            BufferedReader br = new BufferedReader(new FileReader(file));

                            like_name = br.readLine();

                            br.close();

                        } catch (FileNotFoundException e) {
                            System.out.println(e);
                        } catch (IOException e) {
                            System.out.println(e);
                        }

                        try {
                            File file = new File("dat/likelist/" + String.valueOf(id) + ".like");
                            FileWriter fw = new FileWriter(file, true);
                            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
                
                            pw.println(like_id);
                
                            pw.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            File file = new File("dat/likelist/" + like_id + ".like");
                            BufferedReader br = new BufferedReader(new FileReader(file));

                            String str = br.readLine();
                            while (str != null) {
                                int liked_id = Integer.parseInt(str);
                                MainFrame.mainController.PrintLog("[ServerManager] [ID: " + like_id + "] like [ID: " + liked_id + "]");
                                if (id == liked_id) {
                                    String like_msg = "NEWCHAT " + String.valueOf(like_id) + " " + like_name.replace(" ", "_");
                                    myOut.println(like_msg);
                                    like_msg =  "NEWCHAT " + String.valueOf(id) + " " + name.replace(" ", "_");
                                    ServerManager.Send(like_id, like_msg);
                                    break;
                                }
                                str = br.readLine();
                            }

                            br.close();
                        } catch (FileNotFoundException e) {
                            System.out.println(e);
                        } catch (IOException e) {
                            System.out.println(e);
                        }
                        break;

                    case "DISLIKE":
                        int dislike_id = Integer.parseInt(inputTokens[1]);
                        break;
                    
                    case "MESSAGE":
                        int msg_id = Integer.parseInt(inputTokens[1]);
                        String msg_str = "MESSAGE " + String.valueOf(id) + " " + inputTokens[2];
                        ServerManager.Send(msg_id, msg_str);
                        break;

                    case "DISCONNECT":
                        myOut.println("DISCONNECT");
                        isConnecting = false;
                        break;

                    default:
                        MainFrame.mainController.PrintLog("[MesgRecv] Wrong Command : " + cmd);
                        break;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getIsConnecting() {
        return isConnecting;
    }

    public int getMyId () {
        return id;
    }
}

public class ServerManager extends Thread {
    private static int maxConnection;
    private static Socket[] incoming;
    private static InputStreamReader[] isr;
    private static BufferedReader[] in;
    private static PrintWriter[] out;
    private static ServerSocket server;

    private static ClientProcThread[] myClientProcThread;

    private static boolean isLaunching;

    public ServerManager(int maxConnection) {
        this.maxConnection = maxConnection;
        incoming = new Socket[maxConnection];
        isr = new InputStreamReader[maxConnection];
        in = new BufferedReader[maxConnection];
        out = new PrintWriter[maxConnection];

        myClientProcThread = new ClientProcThread[maxConnection];

        isLaunching = true;
    }

    public void run() {
        MainFrame.mainController.PrintLog("[ServerManager] The server has launched.");
        try {
            server = new ServerSocket(10000);

            while (isLaunching) {
                int num = CheckFlag();

                if (num < 0) {
                    MainFrame.mainController.PrintLog("[ServerManager] Capasity Over.");
                    while (!(num < 0))
                        num = CheckFlag();
                }
                MainFrame.mainController.PrintLog("[ServerManager] Waiting for connection...");
                incoming[num] = server.accept();
                MainFrame.mainController.PrintLog("[ServerManager] Accept client No." + num);

                isr[num] = new InputStreamReader(incoming[num].getInputStream());
                in[num] = new BufferedReader(isr[num]);
                out[num] = new PrintWriter(incoming[num].getOutputStream(), true);

                myClientProcThread[num] = new ClientProcThread(num, incoming[num], isr[num], in[num], out[num]);// 必要なパラメータを渡しスレッドを作成
                myClientProcThread[num].start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        MainFrame.mainController.PrintLog("[ServerManager] The server has stopped.");
    }

    public static void Send (int id, String str) {
        MainFrame.mainController.PrintLog("[ServerManger] Send for " + String.valueOf(id) + " : " + str);
        for (int i=0; i<maxConnection; i++) {
            if (myClientProcThread[i] == null) {
                //MainFrame.mainController.PrintLog("[ServerMangaer] " + i + " is not exist");
            } else if (myClientProcThread[i].getIsConnecting() && myClientProcThread[i].getMyId() == id) {
                MainFrame.mainController.PrintLog("[ServerMangaer] " + i + " is correct");
                myClientProcThread[i].myOut.println(str);
                return;
            }
        }

        MainFrame.mainController.PrintLog("[ServerManager] [ID : " + id + "] is not exist.");
        try {
            File file = new File("dat/reservation/" + String.valueOf(id) + ".resv");
            FileWriter fw = new FileWriter(file, true);
            PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

            pw.println(str);

            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SendAll(String str) {
        MainFrame.mainController.PrintLog("[ServerManger] SendAll : " + str);
        for (int i=0; i<maxConnection; i++) {
            if (myClientProcThread[i] == null) continue;
            if (myClientProcThread[i].getIsConnecting() == true) {
                out[i].println(str);
                out[i].flush();
                MainFrame.mainController.PrintLog("[ServerManager] SendMessage for [" + i + "]");
            }
        }
    }

    private int CheckFlag () {
        for (int i=0; i < maxConnection; i++) {
            if (myClientProcThread[i] == null) return i;
            else if (!(myClientProcThread[i].getIsConnecting())) return i;
        }
        return -1;
    }

    public void ServerStop () {
        isLaunching = false;
        try {server.close();}catch(IOException e){e.printStackTrace();}
    }
}