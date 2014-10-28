package com.cometproject.server.network.security.hurlant;

import org.jboss.netty.buffer.ChannelBuffer;

public class ARC4 {
    private int i = 0;
    private int j = 0;
    private int[] table = new int[256];

    public ARC4(byte[] key) {
        this.init(key);
    }

    public void init(byte[] a) {
        int k = a.length;
        this.i = 0;

        while (this.i < 256) {
            this.table[this.i] = this.i;
            this.i++;
        }
        this.j = 0;
        this.i = 0;
        while (this.i < 256) {
            this.j = ((this.j + this.table[this.i]) + (a[this.i % k] & 0xff)) % 256;
            this.swap(this.i, this.j);
            this.i++;
        }
        this.i = 0;
        this.j = 0;
    }

    public ChannelBuffer parse(ChannelBuffer channelBuffer) {
        for (int a = 0; a < channelBuffer.readableBytes(); a++) {
            this.i = (this.i + 1) % 256;
            this.j = (this.j + this.table[this.i]) % 256;
            this.swap(this.i, this.j);
            channelBuffer.setByte(a, (byte) ((channelBuffer.getByte(a) & 0xff) ^ this.table[(this.table[this.i] + this.table[this.j]) % 256]));
        }

        return channelBuffer;
    }

    private void swap(int a1, int a2) {
        int k = this.table[a1];
        this.table[a1] = this.table[a2];
        this.table[a2] = k;
    }
}
