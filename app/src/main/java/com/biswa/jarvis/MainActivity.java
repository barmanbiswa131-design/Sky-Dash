package com.biswa.jarvis;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import android.content.Context;
import java.util.*;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setTitle("Maze Rush");
        setContentView(new MazeGameView(this));
    }

    static class MazeGameView extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Random rng = new Random();
        int cols = 21, rows = 21;
        boolean[][] walls = new boolean[rows][cols];
        float px, py, exitX, exitY;
        ArrayList<Bot> bots = new ArrayList<>();
        long startAt;
        int state = 0; // 0 lobby, 1 countdown, 2 playing, 3 finished
        int countdown = 8;
        int finishRank = -1;
        long lastBotMove;
        float downX, downY;

        static class Bot { float x,y; boolean finished; Bot(float x,float y){this.x=x;this.y=y;} }

        MazeGameView(Context c) { super(c); p.setTypeface(Typeface.create("sans", Typeface.BOLD)); setBackgroundColor(Color.rgb(7,10,16)); }

        void newMatch() {
            generateMaze();
            px=1; py=1; exitX=cols-2; exitY=rows-2;
            bots.clear();
            for(int i=0;i<9;i++) bots.add(new Bot(1,1));
            state=1; startAt=System.currentTimeMillis(); finishRank=-1; invalidate();
        }

        void generateMaze() {
            for(int y=0;y<rows;y++) Arrays.fill(walls[y], true);
            carve(1,1);
            walls[1][1]=false; walls[rows-2][cols-2]=false;
        }

        void carve(int x,int y) {
            walls[y][x]=false;
            int[][] ds={{2,0},{-2,0},{0,2},{0,-2}};
            for(int i=ds.length-1;i>0;i--){int j=rng.nextInt(i+1);int[] t=ds[i];ds[i]=ds[j];ds[j]=t;}
            for(int[] d:ds){int nx=x+d[0],ny=y+d[1];
                if(nx>0&&nx<cols-1&&ny>0&&ny<rows-1&&walls[ny][nx]){walls[y+d[1]/2][x+d[0]/2]=false;carve(nx,ny);}
            }
        }

        boolean free(float x,float y){int ix=Math.round(x),iy=Math.round(y);return ix>=0&&iy>=0&&ix<cols&&iy<rows&&!walls[iy][ix];}

        void movePlayer(float dx,float dy){
            if(state!=2)return;
            float nx=px+dx,ny=py+dy;
            if(free(nx,ny)){px=nx;py=ny;}
            if(Math.round(px)==Math.round(exitX)&&Math.round(py)==Math.round(exitY)){finishRank=1;state=3;}
            invalidate();
        }

        void updateBots(){
            if(state!=2)return;
            long now=System.currentTimeMillis(); if(now-lastBotMove<180)return; lastBotMove=now;
            int finished=0;
            for(Bot b:bots){
                if(b.finished){finished++;continue;}
                float dx=exitX-b.x,dy=exitY-b.y;
                float ax=Math.abs(dx)>Math.abs(dy)?Math.signum(dx):0;
                float ay=ax==0?Math.signum(dy):0;
                ArrayList<float[]> choices=new ArrayList<>();
                choices.add(new float[]{ax,ay}); choices.add(new float[]{ay,ax}); choices.add(new float[]{-ay,ax}); choices.add(new float[]{-ax,-ay});
                Collections.shuffle(choices,rng);
                for(float[] c:choices){if(free(b.x+c[0],b.y+c[1])){b.x+=c[0];b.y+=c[1];break;}}
                if(Math.round(b.x)==Math.round(exitX)&&Math.round(b.y)==Math.round(exitY))b.finished=true;
            }
            if(state==3)return;
            invalidate();
        }

        @Override protected void onDraw(Canvas c){
            super.onDraw(c); int w=getWidth(),h=getHeight();
            p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(7,10,16));c.drawRect(0,0,w,h,p);
            p.setColor(Color.WHITE);p.setTextSize(30);c.drawText("MAZE RUSH",24,42,p);
            p.setTextSize(14);p.setColor(Color.LTGRAY);c.drawText("10 PLAYERS  •  FIND THE EXIT",24,66,p);
            if(state==0){drawLobby(c,w,h);return;}
            int top=82; float cell=Math.min((w-24f)/cols,(h-top-90f)/rows);float ox=(w-cols*cell)/2f;
            for(int y=0;y<rows;y++)for(int x=0;x<cols;x++){p.setColor(walls[y][x]?Color.rgb(31,39,52):Color.rgb(12,17,25));c.drawRect(ox+x*cell,top+y*cell,ox+(x+1)*cell-1,top+(y+1)*cell-1,p);}
            p.setColor(Color.rgb(50,220,120));c.drawRect(ox+exitX*cell,top+exitY*cell,ox+(exitX+1)*cell,top+(exitY+1)*cell,p);
            p.setColor(Color.rgb(80,170,255));c.drawCircle(ox+(px+.5f)*cell,top+(py+.5f)*cell,cell*.32f,p);
            p.setColor(Color.rgb(245,90,90));for(Bot b:bots)if(!b.finished)c.drawCircle(ox+(b.x+.5f)*cell,top+(b.y+.5f)*cell,cell*.25f,p);
            if(state==1){int left=Math.max(0,countdown-(int)((System.currentTimeMillis()-startAt)/1000));p.setTextSize(48);p.setColor(Color.WHITE);c.drawText("START " + left,w/2-80,top+rows*cell/2,p);if(left==0){state=2;lastBotMove=0;}postInvalidateDelayed(100);}
            if(state==2){p.setTextSize(16);p.setColor(Color.WHITE);c.drawText("Swipe to move • Green = EXIT",24,h-34,p);updateBots();}
            if(state==3){p.setColor(Color.argb(230,5,8,12));c.drawRect(0,0,w,h,p);p.setColor(Color.WHITE);p.setTextSize(42);c.drawText("FINISHED!",w/2-115,h/2-30,p);p.setTextSize(24);c.drawText("RANK #"+finishRank,w/2-65,h/2+15,p);p.setTextSize(16);c.drawText("Tap to play again",w/2-70,h/2+55,p);}
        }

        void drawLobby(Canvas c,int w,int h){p.setColor(Color.WHITE);p.setTextSize(28);c.drawText("10 PLAYER ESCAPE",24,130,p);p.setColor(Color.LTGRAY);p.setTextSize(17);c.drawText("Waiting room",24,165,p);p.setColor(Color.rgb(70,190,255));c.drawRoundRect(24,190,w-24,255,18,18,p);p.setColor(Color.BLACK);p.setTextSize(20);c.drawText("PLAY",w/2-28,231,p);p.setColor(Color.GRAY);p.setTextSize(14);c.drawText("Players are filled with bots when needed.",24,295,p);c.drawText("No database • first playable prototype",24,320,p);}

        @Override public boolean onTouchEvent(android.view.MotionEvent e){
            if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getX();downY=e.getY();if(state==0){newMatch();return true;}if(state==3){newMatch();return true;}return true;}
            if(e.getAction()==MotionEvent.ACTION_UP&&state==2){float dx=e.getX()-downX,dy=e.getY()-downY;if(Math.max(Math.abs(dx),Math.abs(dy))<20)return true;if(Math.abs(dx)>Math.abs(dy))movePlayer(Math.signum(dx),0);else movePlayer(0,Math.signum(dy));return true;}return true;
        }
    }
}
