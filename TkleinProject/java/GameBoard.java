package com.klein.tyelor.tkleinmillipede;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

public class GameBoard extends View implements View.OnTouchListener, MediaPlayer.OnCompletionListener {
    private int width = 440, height = 440;
    private static final float SIZEX = 576.0f, SIZEY = 576.0f;
    private static int timerDelay = 33;
    private static final int DEFAULTCOLOR = Color.rgb(0, 0, 0);
    private Paint pausedPaint;
    private TextPaint textPaint;
    private static final int imageSize = 24;
    private boolean isRunning = false;
    private Handler handler;
    private Runnable timer;
    private Bitmap[] mushroomStates = new Bitmap[4], millipedeStates = new Bitmap[2];
    private Bitmap playerShip, bullet, oneUp;
    private int[][] gameBoard = new int[24][24];
    private int[] killSounds = new int[12];
    private int numRocksSetting = 15, snakeSegsSetting = 5, numLivesSetting = 3, soundVolumeSetting = 50, numSnakesSetting = 2;
    private boolean playSoundsSetting = true, allowPowerUpsSetting = true, resetOnDeath = true;
    private boolean isPaused = true;
    private ArrayList<Millipede> millipedes;
    private int snakeTicks, powerUpTicks;
    private int millipedeSpeed = 5, powerUpSpeed = 16;
    private int score, lives, kills, headshots;
    private Player thePlayer;
    private Bullet theBullet;
    private PowerUp thePowerUp;
    private TextView scoreAndLifeText;
    private MediaPlayer mediaPlayer;
    private int currentMediaID;
    private boolean playerHit;

    public GameBoard(Context context) {
        super(context);
        this.initGameBoard();
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public GameBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initGameBoard();
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public GameBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initGameBoard();
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    private void initGameBoard() {
        snakeTicks = 0;

        mushroomStates[0] = BitmapFactory.decodeResource(getResources(), R.drawable.mushroom_state0);
        mushroomStates[1] = BitmapFactory.decodeResource(getResources(), R.drawable.mushroom_state1);
        mushroomStates[2] = BitmapFactory.decodeResource(getResources(), R.drawable.mushroom_state2);
        mushroomStates[3] = BitmapFactory.decodeResource(getResources(), R.drawable.mushroom_state3);
        millipedeStates[0] = BitmapFactory.decodeResource(getResources(), R.drawable.millipede_face);
        millipedeStates[1] = BitmapFactory.decodeResource(getResources(), R.drawable.millipede_segment);
        killSounds[0] = R.raw.fatality;
        killSounds[1] = R.raw.doublekill;
        killSounds[2] = R.raw.multikill;
        killSounds[3] = R.raw.megakill;
        killSounds[4] = R.raw.ultrakill;
        killSounds[5] = R.raw.monsterkill;
        killSounds[6] = R.raw.killingspree;
        killSounds[7] = R.raw.rampage;
        killSounds[8] = R.raw.dominating;
        killSounds[9] = R.raw.unstoppable;
        killSounds[10] = R.raw.godlike;
        killSounds[11] = R.raw.unreal;
        playerShip = BitmapFactory.decodeResource(getResources(), R.drawable.player_ship);
        bullet = BitmapFactory.decodeResource(getResources(), R.drawable.bullet);
        oneUp = BitmapFactory.decodeResource(getResources(), R.drawable.oneup);

        pausedPaint = new Paint(DEFAULTCOLOR);
        pausedPaint.setAlpha(128);

        textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(30);
        textPaint.setAntiAlias(true);
        textPaint.setAlpha(255);

        for(int i = 0; i < numRocksSetting; i++) {
            int x = (int)(Math.random() * 24), y = (int)(Math.random() * 22) + 1;
            gameBoard[x][y] = 4;
        }

        handler = new Handler() ;
        timer = new Runnable() {
            @Override
            public void run() {
                onTimer();
                if (isRunning)
                    handler.postDelayed(this, timerDelay) ;
            }
        };

        thePlayer = new Player(11);

        millipedes = new ArrayList<>();
        if(numSnakesSetting == 2)
            millipedes.add(new Millipede(0, 0, snakeSegsSetting, false));
        millipedes.add(new Millipede(23, 0, snakeSegsSetting, true));
        score = 0;
        lives = 3;
        kills = 0;
        headshots = 0;
        setOnTouchListener(this);
        theBullet = null;
        thePowerUp = null;
        playerHit = false;
    }

    private void onTimer() {
        if(!isPaused && isRunning) {
            snakeTicks = (snakeTicks + 1) % millipedeSpeed;
            powerUpTicks = (powerUpTicks + 1) % powerUpSpeed;
            if(allowPowerUpsSetting && thePowerUp == null && snakeTicks == 0) {
                int rand = (int)(Math.random() * ((9999) + 1));

                if(rand >= 9975)
                    thePowerUp = new PowerUp((int)(Math.random() * ((23) + 1)), -1, 0);
            }

            if(theBullet != null) {
                theBullet.move();

                if(theBullet != null && theBullet.y < 0)
                    theBullet = null;
            }

            if (snakeTicks == 0 && !millipedes.isEmpty())
                try {
                    for (Millipede millipede : millipedes) {
                        millipede.move();
                    }
                } catch(ConcurrentModificationException ignored) { }

            if(thePowerUp != null && powerUpTicks == 0)
                thePowerUp.move();

            if(playerHit) {
                lives--;
                resetBoard(!(lives > 0), true);
            }

            invalidate();
        }
    }

    public void resume() {
        isRunning = true;
        handler.postDelayed(timer, timerDelay);
    }

    public void pause() {
        isRunning = false;
        handler.removeCallbacks(timer) ;
    }

    public void movePlayer(final int x) {
        if(thePlayer != null) {
            thePlayer.move(x);
            if(thePowerUp != null)
                thePowerUp.checkCollision();

            if(!millipedes.isEmpty())
                for(Millipede millipede: millipedes)
                    if(millipede.hitPlayer())
                        playerHit = true;
        }
    }

    public void fireBullet() {
        if(!isPaused && theBullet == null && thePlayer != null) {
            playClip(R.raw.firebullet);
            theBullet = new Bullet(thePlayer.x);
        }
    }

    public void connectScoreText(final TextView scoreText) {
        this.scoreAndLifeText = scoreText;
    }

    public void checkVictory() {
        if(millipedes.size() == 0) {
            playClip(getKillSound());

            resetBoard(false, false);
        }
    }

    public void resetBoard(final boolean resetScore, final boolean wasLose) {
        if(resetScore || resetOnDeath) {
            if(wasLose && resetScore)
                ((A_GameActivity) getContext()).reportLose(kills, score);

            clearGameBoard(resetScore);

            for(int i = 0; i < numRocksSetting; i++) {
                int x = (int)(Math.random() * 24), y = (int)(Math.random() * 22) + 1;
                gameBoard[x][y] = 4;
            }
        } else if(!wasLose)
            kills += numSnakesSetting;

        for (Iterator<Millipede> iterator = millipedes.iterator(); iterator.hasNext(); ) {
            Millipede millipede = iterator.next();
            millipede.segments.clear();
            iterator.remove();
        }

        playerHit = false;

        if(numSnakesSetting == 2)
            millipedes.add(new Millipede(0, 0, snakeSegsSetting, false));

        millipedes.add(new Millipede(23, 0, snakeSegsSetting, true));

        invalidate();
    }

    public void clearGameBoard(final boolean resetScore) {
        for(int x = 0; x < 24; x++)
            for(int y = 0; y < 24; y++)
                gameBoard[x][y] = 0;

        theBullet = null;
        thePowerUp = null;
        thePlayer = new Player(11);
        isPaused = true;
        if(resetScore) {
            score = 0;
            lives = numLivesSetting;
            kills = 0;
        }
    }

    public void setSettings(final int snakeSegs, final int numRocks, final int numLives, final int snakeSpeed,
                            final boolean snakeNum, final int soundVolume, final boolean playSounds, final boolean powerUpsOn,
                            final int powerUpDelay, final boolean deathReset) {
        int numSnakes = ((snakeNum) ? 2 : 1);
        if(snakeSegs != snakeSegsSetting || numRocks != numRocksSetting || numLives != numLivesSetting
                || millipedeSpeed != snakeSpeed || numSnakesSetting != numSnakes || allowPowerUpsSetting != powerUpsOn
                || powerUpSpeed != powerUpDelay || resetOnDeath != deathReset) {
            snakeSegsSetting = snakeSegs;
            numRocksSetting = numRocks;
            numLivesSetting = numLives;
            millipedeSpeed = snakeSpeed;
            numSnakesSetting = numSnakes;
            allowPowerUpsSetting = powerUpsOn;
            powerUpSpeed = powerUpDelay;
            resetOnDeath = deathReset;

            resetBoard(true, false);
        }
        playSoundsSetting = playSounds;
        soundVolumeSetting = soundVolume;
        if(mediaPlayer != null)
            mediaPlayer.setVolume(soundVolumeSetting * 0.01f, soundVolumeSetting * 0.01f);
    }

    public int getKillSound() {
        int tKills = Math.min(kills, 11);

        return killSounds[tKills];
    }

    public int getHeadshotSound() {
        if(headshots > 50)
            return R.raw.headhunter;
        else
            return R.raw.headshot;
    }

    public int getPlayerDeathSound() {
        if(lives > 1)
            return R.raw.playerdie;
        else if(kills == 0 && score < 200)
            return R.raw.bottomfeeder;
        else if ((kills > 5 && kills < 20) || (score >= 1000 && score < 5000))
            return R.raw.impressive;
        else if(kills >= 20 || score >= 5000)
            return R.raw.godlike;
        else
            return R.raw.playerdie;
    }

    public void playClip(int id) {
        if(playSoundsSetting) {
            if (mediaPlayer != null && id == currentMediaID) {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
            } else {
                if (mediaPlayer != null)
                    mediaPlayer.release();

                currentMediaID = id;
                mediaPlayer = MediaPlayer.create(getContext(), id);
                mediaPlayer.setOnCompletionListener(this);
                mediaPlayer.setVolume(soundVolumeSetting * 0.01f, soundVolumeSetting * 0.01f);
                mediaPlayer.start();
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer amp) {
        amp.release();
        mediaPlayer = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float scaleX = this.width / SIZEX, scaleY = this.height / SIZEY;
        canvas.drawRGB(0, 0, 0);
        canvas.scale(scaleX, scaleY);

        for(int i = 0; i < 24; i++)
            for(int j = 0; j < 24; j++) {
                int x = i * imageSize, y = j * imageSize, state = gameBoard[i][j] - 1;
                if(state != -1)
                    canvas.drawBitmap(mushroomStates[state], null, new Rect(x, y, x + imageSize, y + imageSize), null);
            }

        for(Millipede millipede: millipedes)
            millipede.draw(canvas);

        if(theBullet != null)
            theBullet.draw(canvas);

        if(thePowerUp != null)
            thePowerUp.draw(canvas);

        if(thePlayer != null)
            thePlayer.draw(canvas);

        if(isPaused) {
            canvas.drawRect(0, 0, this.width, this.height, pausedPaint);
            int x = (int) (this.width / (scaleX * 2)), y = (int) (this.height / (scaleY * 2));
            canvas.drawText("PAUSED", x, y, textPaint);
        }

        if(scoreAndLifeText != null)
            scoreAndLifeText.setText("Lives: " + lives + "   Score: " + score);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle ret = new Bundle();
        for(int i = 0; i < 24; i++)
            ret.putSerializable("gameBoard" + i, gameBoard[i]);

        ret.putSerializable("millipedes", millipedes);

        int[] coords = new int[2];
        if(theBullet != null) {
            coords[0] = theBullet.x;
            coords[1] = theBullet.y;
        } else {
            coords[0] = -1;
            coords[1] = -1;
        }
        ret.putIntArray("bulletCoords", coords);

        coords = new int[3];
        if(thePowerUp != null) {
            coords[0] = thePowerUp.x;
            coords[1] = thePowerUp.y;
            coords[2] = thePowerUp.powerUpState;
        } else {
            coords[0] = -1;
            coords[1] = -1;
            coords[2] = -1;
        }
        ret.putIntArray("powerUpCoords", coords);

        ret.putInt("playerX", thePlayer.x);
        ret.putInt("lives", lives);
        ret.putInt("score", score);
        ret.putInt("headshots", headshots);
        ret.putInt(getContext().getString(R.string.prefs_SoundVolume), soundVolumeSetting);
        ret.putInt(getContext().getString(R.string.prefs_SnakeNum), numSnakesSetting);
        ret.putInt(getContext().getString(R.string.prefs_SnakeSpeed), millipedeSpeed);
        ret.putBoolean(getContext().getString(R.string.prefs_ResetOnDeath), resetOnDeath);
        ret.putBoolean(getContext().getString(R.string.prefs_PlaySounds), playSoundsSetting);
        ret.putBoolean(getContext().getString(R.string.prefs_PowerUpsOn), allowPowerUpsSetting);
        ret.putInt(getContext().getString(R.string.prefs_PowerUpsSpeed), powerUpSpeed);
        ret.putInt(getContext().getString(R.string.prefs_SnakeSegs), snakeSegsSetting);
        ret.putInt(getContext().getString(R.string.prefs_NumRocks), numRocksSetting);
        ret.putInt(getContext().getString(R.string.prefs_NumLives), numLivesSetting);

        ret.putParcelable("default", super.onSaveInstanceState());

        return ret;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        initGameBoard();

        Bundle ret = (Bundle) state;
        super.onRestoreInstanceState(ret.getParcelable("default"));

        for(int i = 0; i < 24; i++)
            gameBoard[i] = (int[]) ret.getSerializable("gameBoard" + i);

        millipedes = (ArrayList<Millipede>) ret.getSerializable("millipedes");

        ArrayList<Millipede> temp = (ArrayList<Millipede>) ret.getSerializable("millipedes");
            if(temp != null)
                for(Millipede millipede: temp)
                    millipede.fixParent(this);

        int[] coords = ret.getIntArray("bulletCoords");
        if(coords != null && coords[0] != -1 && coords[1] != -1) {
            theBullet = new Bullet(coords[0]);
            theBullet.y = coords[1];
        }

        coords = ret.getIntArray("powerUpCoords");
        if(coords != null && coords[0] != -1 && coords[1] != -1 && coords[2] != -1)
            thePowerUp = new PowerUp(coords[0], coords[1], coords[2]);


        thePlayer.x = ret.getInt("playerX");
        lives = ret.getInt("lives");
        score = ret.getInt("score");
        headshots = ret.getInt("headshots");
        playSoundsSetting = ret.getBoolean(getContext().getString(R.string.prefs_PlaySounds));
        allowPowerUpsSetting = ret.getBoolean(getContext().getString(R.string.prefs_PowerUpsOn));
        resetOnDeath = ret.getBoolean(getContext().getString(R.string.prefs_ResetOnDeath));
        powerUpSpeed = ret.getInt(getContext().getString(R.string.prefs_PowerUpsSpeed));
        soundVolumeSetting = ret.getInt(getContext().getString(R.string.prefs_SoundVolume));
        numSnakesSetting = ret.getInt(getContext().getString(R.string.prefs_SnakeNum));
        millipedeSpeed = ret.getInt(getContext().getString(R.string.prefs_SnakeSpeed));
        snakeSegsSetting = ret.getInt(getContext().getString(R.string.prefs_SnakeSegs));
        numRocksSetting = ret.getInt(getContext().getString(R.string.prefs_NumRocks));
        numLivesSetting = ret.getInt(getContext().getString(R.string.prefs_NumLives));
        isPaused = true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec), height = MeasureSpec.getSize(heightMeasureSpec);
        float factor = SIZEX / SIZEY, cWidth = height * factor, cHeight = width / factor;

        if(cWidth > width) {
            int tempW = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    tempH = MeasureSpec.makeMeasureSpec((int)cHeight, MeasureSpec.EXACTLY);
            setMeasuredDimension(tempW, tempH);
        } else {
            int tempW = MeasureSpec.makeMeasureSpec((int)cWidth, MeasureSpec.EXACTLY),
                    tempH = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            setMeasuredDimension(tempW, tempH);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isPaused = !isPaused;
                if(isPaused)
                    playClip(R.raw.closemenu);
                else
                    playClip(R.raw.openmenu);
                invalidate();
                break;
            default:
                break;
        }

        return true;
    }

    private class Millipede implements Serializable {
        private static final long serialVersionUID = 8610918104719398L;
        private boolean isLeft;
        private ArrayList<Segment> segments;

        private Millipede(final int x, final int y, final int size, final boolean isLeft) {
            this.isLeft = isLeft;
            segments = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                int direction = ((isLeft) ? 1 : -1);
                if (i > 0) {
                    Segment temp = new Segment(x + i * direction, y, 1, 0);
                    temp.setFacing();

                    segments.add(temp);
                } else {
                    Segment temp = new Segment(x + i * direction, y, 0, 90 - 90 * direction);

                    segments.add(temp);
                }
            }
        }

        private void move() {
            if(notBlocked()) {
                int direction = ((this.isLeft) ? -1 : 1), lastX = -1, lastY = -1, lastFacing = -1;

                for (Segment segment : segments) {
                    if (lastX != -1) {
                        int temp = segment.x;
                        segment.x = lastX;
                        lastX = temp;
                        temp = segment.y;
                        segment.y = lastY;
                        lastY = temp;
                        temp = segment.facingAngle;
                        segment.facingAngle = lastFacing;
                        lastFacing = temp;
                    } else {
                        lastX = segment.x;
                        lastY = segment.y;
                        lastFacing = segment.facingAngle;
                        segment.x += direction;
                        segment.setFacing();
                    }
                }
            } else if(hitPlayer()) {
                playClip(getPlayerDeathSound());
                playerHit = true;
            } else {
                this.isLeft = !this.isLeft;
                int direction = ((this.isLeft) ? -1 : 1), lastX = -1, lastY = -1, lastFacing = -1;

                for (Segment segment : segments) {
                    if (lastX != -1) {
                        int temp = segment.x;
                        segment.x = lastX;
                        lastX = temp;
                        temp = segment.y;
                        segment.y = lastY;
                        lastY = temp;
                        temp = segment.facingAngle;
                        segment.facingAngle = lastFacing;
                        lastFacing = temp;
                    } else {
                        lastX = segment.x;
                        lastY = segment.y;
                        lastFacing = segment.facingAngle;
                        if (!onBottom()) {
                            segment.y += 1;
                            segment.facingAngle = 270;
                        } else {
                            segment.x += direction;
                            segment.setFacing();
                        }
                    }
                }
            }
        }

        private boolean notBlocked() {
            if(!segments.isEmpty()) {
                int direction = ((this.isLeft) ? -1 : 1), x = segments.get(0).x + direction, y = segments.get(0).y;

                return !(x + direction < -1 || y < -1 || x + direction > 24 || y > 24 || gameBoard[x][y] > 0 || hitPlayer());
            }

            removeSelf();
            return false;
        }

        private boolean onBottom() {
            return segments.get(0).y == 23;
        }

        private boolean hitPlayer() {
            if(thePlayer != null)
                for(Segment segment: segments)
                    if (segment.x == thePlayer.x && segment.y == 23)
                    return true;


            return false;
        }

        private void draw(final Canvas canvas) {
            for(Segment segment: segments)
                segment.draw(canvas);
        }

        private void removeSelf() {
            segments.clear();
            millipedes.remove(this);
        }

        private void fixParent(GameBoard parent) {
            try {
                Field field = Millipede.class.getDeclaredField("this$0");
                field.setAccessible(true);
                field.set(this, parent);
            } catch (Exception e) {
                resetBoard(true, false);
            }
        }

        private void writeObject(ObjectOutputStream stream) throws IOException {
            stream.writeBoolean(isLeft);
            stream.writeObject(segments);
        }

        private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            isLeft = stream.readBoolean();
            segments = new ArrayList<>();
            ArrayList<Segment> temp = (ArrayList<Segment>) stream.readObject();
            for(Segment segment: temp)
                segments.add(new Segment(segment.x, segment.y, segment.state, segment.facingAngle));
        }

        private class Segment implements Serializable {
            private static final long serialVersionUID = -6615410735400353626L;
            private int x, y;
            private int state;
            private int facingAngle;

            private Segment(final int x, final int y, final int state, final int facingAngle) {
                this.x = x;
                this.y = y;
                this.state = state;
                this.facingAngle = facingAngle;
            }

            private void draw(final Canvas canvas) {
                int tx = x * imageSize, ty = y * imageSize;
                canvas.save();
                canvas.rotate(facingAngle, tx + (imageSize/2), ty + (imageSize/2));
                canvas.drawBitmap(millipedeStates[state], null, new Rect(tx, ty, tx + imageSize, ty + imageSize), null);
                canvas.restore();
            }

            private void setFacing() {
                if(notBlocked()) {
                    if (isLeft)
                        facingAngle = 0;
                    else
                        facingAngle = 180;
                }
            }

            private void fixParent(Millipede parent) {
                try {
                    Field field = Segment.class.getDeclaredField("this$1");
                    field.setAccessible(true);
                    field.set(this, parent);
                } catch (Exception e) {
                    resetBoard(true, false);
                }
            }

            private void split() {
                int splitIndex = -1, newSize, newX = -1, newY = -1;
                for(int i = 0; i < segments.size(); i++) {
                    if(segments.get(i).equals(this))
                        splitIndex = i;
                }

                newSize = segments.size() - splitIndex - 1;
                ArrayList<Segment> segs = new ArrayList<>();

                int count = 0;
                for (Iterator<Segment> iterator = segments.iterator(); iterator.hasNext(); ) {
                    Segment segment = iterator.next();
                    if(count >= splitIndex) {
                        if(count == splitIndex + 1) {
                            newX = segment.x;
                            newY = segment.y;
                        }
                        segs.add(segment);
                        iterator.remove();
                    }

                    count++;
                }

                if(splitIndex == 0)
                    removeSelf();

                if(newSize > 0 && newX != -1 && newY != -1) {
                    Millipede temp = new Millipede(newX, newY, 1, isLeft);
                    segs.remove(0);
                    segs.get(0).state = 0;
                    for(Segment segment: segs)
                        segment.fixParent(temp);

                    temp.segments = segs;
                    millipedes.add(temp);
                }

                checkVictory();
            }

            private void writeObject(ObjectOutputStream stream)
                    throws IOException {
                stream.writeInt(x);
                stream.writeInt(y);
                stream.writeInt(state);
                stream.writeInt(facingAngle);
            }

            private void readObject(ObjectInputStream stream)
                    throws IOException, ClassNotFoundException {
                x = stream.readInt();
                y = stream.readInt();
                state = stream.readInt();
                facingAngle = stream.readInt();
            }
        }
    }

    private class Player {
        int x;

        Player(final int x) {
            this.x = x;
        }

        private void move(final int x) {
            if(!isPaused)
                this.x = Math.min(Math.max(this.x + x, 0), 23);
        }

        private void draw(final Canvas canvas) {
            int tx = x * imageSize, ty = 23 * imageSize;
            canvas.drawBitmap(playerShip, null, new Rect(tx, ty, tx + imageSize, ty + imageSize), null);
        }
    }

    private class Bullet {
        int x, y;

        private Bullet(final int x) {
            this.x = x;
            this.y = 23;
        }

        private void move() {
            if(!isPaused) {
                this.y--;
                checkCollision();
            }
        }

        private void checkCollision() {
            if(this.y <= -1)
                theBullet = null;
            else if(gameBoard[x][y] != 0) {
                gameBoard[x][y]--;
                theBullet = null;
                score += 5;
                playClip(R.raw.mushhit);
            }
            Millipede.Segment temp = null;

            if(theBullet != null) {
                for (Millipede millipede : millipedes) {
                    for (Millipede.Segment segment : millipede.segments) {
                        if (x == segment.x && y == segment.y) {
                            theBullet = null;
                            score += 10;
                            gameBoard[x][y] = 4;
                            if (segment.equals(millipede.segments.get(0))) {
                                headshots++;
                                playClip(getHeadshotSound());
                            } else
                                playClip(R.raw.seghit);
                            temp = segment;
                            break;
                        }
                    }
                }
            }

            if(temp != null)
                temp.split();
        }

        private void draw(final Canvas canvas) {
            int tx = x * imageSize, ty = y * imageSize;
            canvas.drawBitmap(bullet, null, new Rect(tx, ty, tx + imageSize, ty + imageSize), null);
        }
    }

    private class PowerUp {
        int x, y;
        int powerUpState;

        PowerUp(final int x, final int y, final int powerUpState) {
            this.x = x;
            this.y = y;
            this.powerUpState = powerUpState;
        }

        private void move() {
            if(!isPaused) {
                this.y++;
                checkCollision();
            }
        }

        private void checkCollision() {
            if(thePlayer != null && this.x == thePlayer.x && this.y == 23) {
                thePowerUp = null;
                lives++;
                score += 50;
                playClip(R.raw.getoneup);
            } else if (this.y > 24)
                thePowerUp = null;
        }

        private void draw(final Canvas canvas) {
            int tx = x * imageSize, ty = y * imageSize;
            canvas.drawBitmap(oneUp, null, new Rect(tx, ty, tx + imageSize, ty + imageSize), null);
        }
    }
}
