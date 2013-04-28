package de.cubeisland.cubeengine.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;

public class FIFOInterface
{
    private final Core core;
    private final ThreadFactory threadFactory;
    private final File folder;
    private final Charset charset;
    private final FIFOCommandSender sender;

    private final File inputFile;
    private FileInputStream inputStream;
    private FileChannel inputChannel;
    private Thread inputThread;
    private final Queue<String> incomingCommands;

    private final File outputFile;
    private FileOutputStream outputStream;
    private FileChannel outputChannel;
    private Thread outputThread;
    private final BlockingQueue<String> outgoingMessages;

    public FIFOInterface(Core core, File folder, Charset charset)
    {
        assert folder.exists() && folder.isDirectory(): "The folder must exist and actually be a directory!";

        this.core = core;
        this.threadFactory = core.getTaskManager().getThreadFactory();
        this.folder = folder;
        this.charset = charset;
        this.sender = new FIFOCommandSender(this, (BukkitCore)core);

        this.inputFile = new File(folder, "input");
        this.inputStream = null;
        this.inputChannel = null;
        this.inputThread = null;
        this.incomingCommands = new ConcurrentLinkedQueue<String>();

        this.outputFile = new File(folder, "output");
        this.outputStream = null;
        this.outputChannel = null;
        this.outputThread = null;
        this.outgoingMessages = new LinkedBlockingQueue<String>();
    }

    public void start() throws IOException
    {
        // INPUT
        if (this.inputFile.exists() && !this.inputFile.delete())
        {
            throw new IOException("Failed to remove the FIFO!");
        }
        if (!this.inputFile.exists() && !this.inputFile.createNewFile())
        {
            throw new IOException("Failed to create the input FIFO!");
        }
        this.inputStream = new FileInputStream(this.inputFile);
        this.inputChannel = this.inputStream.getChannel();
        this.inputThread = this.threadFactory.newThread(new InputReader());
        this.inputThread.start();

        // OUTPUT
        if (this.outputFile.exists() && !this.outputFile.delete())
        {
            throw new IOException("Failed to remove the FIFO!");
        }
        if (!this.outputFile.exists() && !this.outputFile.createNewFile())
        {
            throw new IOException("Failed to create the output FIFO!");
        }
        this.outputStream = new FileOutputStream(this.outputFile);
        this.outputChannel = this.outputStream.getChannel();
        this.outputThread = this.threadFactory.newThread(new OutputWriter());
        this.outputThread.start();
    }

    public void stop()
    {
        // INPUT
        this.inputThread.interrupt();
        try
        {
            this.inputThread.join(500L);
        }
        catch (InterruptedException ignored)
        {}
        this.inputThread = null;
        try
        {
            this.inputChannel.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        this.inputChannel = null;
        try
        {
            this.inputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        this.inputStream = null;

        // OUTPUT
        this.outputThread.interrupt();
        try
        {
            this.outputThread.join(500L);
        }
        catch (InterruptedException ignored)
        {}
        this.outputThread = null;
        try
        {
            this.outputChannel.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        this.outputChannel = null;
        try
        {
            this.outputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        this.outputStream = null;
    }

    public void writeMessage(String message)
    {
        this.outgoingMessages.offer(message + "\n");
    }

    private class InputReader implements Runnable
    {
        private void executeCommand(final String message)
        {
            core.getTaskManager().callSyncMethod(new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    core.getCommandManager().runCommand(sender, message);
                    return null;
                }
            });
        }

        @Override
        public void run()
        {
            try
            {
                final int bufSize = 256;
                ByteBuffer buf = ByteBuffer.allocate(bufSize);
                int read;
                StringBuilder line = null;
                String message;
                for (;;)
                {
                    buf.rewind();
                    read = inputChannel.read(buf);
                    Thread.sleep(100);
                    if (read > 0)
                    {
                        buf.limit(read);
                        message = new String(buf.array(), 0, read, charset);
                        if (line == null)
                        {
                            line = new StringBuilder();
                        }
                        int newLineOffset;
                        while ((newLineOffset = message.indexOf('\n')) > -1)
                        {
                            line.append(message.substring(0, newLineOffset));
                            this.executeCommand(line.toString());
                            message = message.substring(newLineOffset + 1);
                            line = new StringBuilder();
                        }
                        line.append(message);
                    }
                }
            }
            catch (InterruptedException ignored)
            {}
            catch (IOException e)
            {
                e.printStackTrace(System.err);
            }
        }
    }

    private class OutputWriter implements Runnable
    {
        @Override
        public void run()
        {
            int written;
            try
            {
                for (;;)
                {
                    written = outputChannel.write(ByteBuffer.wrap(outgoingMessages.take().getBytes()));
                    outputChannel.position(outputChannel.position() + written);
                }
            }
            catch (InterruptedException ignored)
            {}
            catch (IOException e)
            {
                e.printStackTrace(System.err);
            }
        }
    }
}
