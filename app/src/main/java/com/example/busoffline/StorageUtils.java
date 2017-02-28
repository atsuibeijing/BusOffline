package com.example.busoffline;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

public class StorageUtils {

    private static final String TAG = "StorageUtils";
    private static Toast toast;
    private static TextView toastText;

    public static class StorageInfo {

        public final String path;
        public final boolean internal;
        public final boolean readonly;
        public final int display_number;

        StorageInfo(String path, boolean internal, boolean readonly, int display_number) {
            this.path = path;
            this.internal = internal;
            this.readonly = readonly;
            this.display_number = display_number;
        }

        public String getDisplayName() {
            StringBuilder res = new StringBuilder();
            if (internal) {
                res.append("Internal SD card");
            } else if (display_number > 1) {
                res.append("SD card " + display_number);
            } else {
                res.append("SD card");
            }
            if (readonly) {
                res.append(" (Read only)");
            }
            return res.toString();
        }
    }

    public static List<StorageInfo> getStorageList() {

        List<StorageInfo> list = new ArrayList<StorageInfo>();
        String def_path = Environment.getExternalStorageDirectory().getPath();
        boolean def_path_internal = !Environment.isExternalStorageRemovable();
        String def_path_state = Environment.getExternalStorageState();
        boolean def_path_available = def_path_state.equals(Environment.MEDIA_MOUNTED)
                || def_path_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        boolean def_path_readonly = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
        BufferedReader buf_reader = null;
        try {
            HashSet<String> paths = new HashSet<String>();
            buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            int cur_display_number = 1;
            Log.d(TAG, "/proc/mounts");
            while ((line = buf_reader.readLine()) != null) {
                Log.d(TAG, line);
                if (line.contains("vfat") || line.contains("exfat") || line.contains("/mnt") || line.contains("fuseblk")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String unused = tokens.nextToken(); //device
                    String mount_point = tokens.nextToken(); //mount point
                    if (paths.contains(mount_point)) {
                        continue;
                    }
                    unused = tokens.nextToken(); //file system
                    List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
                    boolean readonly = flags.contains("ro");

                    if (mount_point.equals(def_path)) {
                        paths.add(def_path);
                        list.add(0, new StorageInfo(def_path, def_path_internal, readonly, -1));
                    } else if (line.contains("/dev/block/vold") || line.contains("/dev/block/sdd1")) {
                        if (!line.contains("/mnt/secure")
                                && !line.contains("/mnt/asec")
                                && !line.contains("/mnt/obb")
                                && !line.contains("/dev/mapper")
                                && !line.contains("tmpfs")
                                && !line.contains("/data/media")) {
                            paths.add(mount_point);
                            list.add(new StorageInfo(mount_point, false, readonly, cur_display_number++));
                        }
                    }
                }
            }

            if (!paths.contains(def_path) && def_path_available) {
                list.add(0, new StorageInfo(def_path, def_path_internal, def_path_readonly, -1));
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (buf_reader != null) {
                try {
                    buf_reader.close();
                } catch (IOException ex) {}
            }
        }
        return list;
    }

    public static void makeTextAndShow(final Context context, final String text) {
        if (toast == null) {
            //如果還沒有建立過Toast，才建立
            final ViewGroup toastView = new FrameLayout(context); // 用來裝toastText的容器
            final FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            final GradientDrawable background = new GradientDrawable();
            toastText = new TextView(context);
            toastText.setLayoutParams(flp);
            toastText.setSingleLine(false);
            toastText.setTextSize(18);
            toastText.setTextColor(Color.argb(0xAA, 0xFF, 0xFF, 0xFF)); // 設定文字顏色為有點透明的白色
            background.setColor(Color.argb(0xAA, 0xFF, 0x00, 0x00)); // 設定氣泡訊息顏色為有點透明的紅色
            background.setCornerRadius(20); // 設定氣泡訊息的圓角程度

            toastView.setPadding(30, 30, 30, 30); // 設定文字和邊界的距離
            toastView.addView(toastText);
            toastView.setBackgroundDrawable(background);

            toast = new Toast(context);
            toast.setView(toastView);
        }
        toastText.setText(text);
        toast.setDuration(Toast.LENGTH_LONG);
        new CountDownTimer(9000, 1000)
        {
            public void onTick(long millisUntilFinished) {toast.show();}
            public void onFinish() {toast.show();}
        }.start();
    }
}


/*
        tmpfs /dev tmpfs rw,nosuid,relatime,mode=755 0 0
        devpts /dev/pts devpts rw,relatime,mode=600 0 0
        proc /proc proc rw,relatime 0 0
        sysfs /sys sysfs rw,relatime 0 0
        none /acct cgroup rw,relatime,cpuacct 0 0
        tmpfs /mnt/asec tmpfs rw,relatime,mode=755,gid=1000 0 0
        tmpfs /mnt/obb tmpfs rw,relatime,mode=755,gid=1000 0 0
        none /dev/cpuctl cgroup rw,relatime,cpu 0 0
        /dev/block/mmcblk0p9 /system ext4 ro,noatime,barrier=1,data=ordered 0 0
        /dev/block/mmcblk0p7 /cache ext4 rw,nosuid,nodev,noatime,barrier=1,journal_async_commit,data=ordered 0 0
        /dev/block/mmcblk0p1 /efs ext4 rw,nosuid,nodev,noatime,barrier=1,journal_async_commit,data=ordered 0 0
        /dev/block/mmcblk0p12 /preload ext4 ro,nosuid,nodev,noatime,barrier=1,data=ordered 0 0
        /dev/block/mmcblk0p10 /data ext4 rw,nosuid,nodev,noatime,barrier=1,journal_async_commit,data=ordered,noauto_da_alloc,discard 0 0
        /dev/block/mmcblk0p4 /mnt/.lfs j4fs rw,relatime 0 0
        /sys/kernel/debug /sys/kernel/debug debugfs rw,relatime 0 0
        /dev/block/vold/259:3 /storage/sdcard0 vfat rw,dirsync,nosuid,nodev,noexec,noatime,nodiratime,uid=1000,gid=1015,fmask=0002,dmask=0002,allow_utime=0020,codepage=cp437,iocharset=iso8859-1,shortname=mixed,utf8,errors=remount-ro,discard 0 0
        /dev/block/vold/179:9 /storage/extSdCard exfat rw,dirsync,nosuid,nodev,noexec,noatime,nodiratime,uid=1000,gid=1023,fmask=0002,dmask=0002,allow_utime=0020,codepage=cp437,iocharset=utf8,namecase=0,errors=remount-ro 0 0
        /dev/block/vold/179:9 /mnt/secure/asec exfat rw,dirsync,nosuid,nodev,noexec,noatime,nodiratime,uid=1000,gid=1023,fmask=0002,dmask=0002,allow_utime=0020,codepage=cp437,iocharset=utf8,namecase=0,errors=remount-ro 0 0
        tmpfs /storage/extSdCard/.android_secure tmpfs ro,relatime,size=0k,mode=000 0 0*/


/*
        rootfs / rootfs ro,seclabel,relatime 0 0
        tmpfs /dev tmpfs rw,seclabel,nosuid,relatime,size=1439040k,nr_inodes=147009,mode=755 0 0
        devpts /dev/pts devpts rw,seclabel,relatime,mode=600 0 0
        proc /proc proc rw,relatime 0 0
        sysfs /sys sysfs rw,seclabel,relatime 0 0
        selinuxfs /sys/fs/selinux selinuxfs rw,relatime 0 0
        /sys/kernel/debug /sys/kernel/debug debugfs rw,relatime 0 0
        none /acct cgroup rw,relatime,cpuacct 0 0
        none /sys/fs/cgroup tmpfs rw,seclabel,relatime,size=1439040k,nr_inodes=147009,mode=750,gid=1000 0 0
        tmpfs /mnt tmpfs rw,seclabel,relatime,size=1439040k,nr_inodes=147009,mode=755,gid=1000 0 0
        none /dev/cpuctl cgroup rw,relatime,cpu 0 0
        /dev/block/mmcblk0p18 /system ext4 ro,seclabel,noatime,norecovery 0 0
        /dev/block/mmcblk0p3 /efs ext4 rw,seclabel,nosuid,nodev,noatime,discard,journal_checksum,journal_async_commit,noauto_da_alloc,data=ordered 0 0
        /dev/block/mmcblk0p19 /cache ext4 rw,seclabel,nosuid,nodev,noatime,discard,journal_checksum,journal_async_commit,noauto_da_alloc,data=ordered 0 0
        /dev/block/mmcblk0p21 /data ext4 rw,seclabel,nosuid,nodev,noatime,discard,journal_checksum,journal_async_commit,noauto_da_alloc,data=ordered 0 0
        /dev/block/mmcblk0p16 /persdata/absolute ext4 rw,seclabel,nosuid,nodev,relatime,data=ordered 0 0
        tmpfs /storage tmpfs rw,seclabel,relatime,size=1439040k,nr_inodes=147009,mode=755,gid=1000 0 0
        /dev/block/loop0 /su ext4 rw,seclabel,noatime,data=ordered 0 0
        /dev/fuse /mnt/runtime/default/emulated fuse rw,nosuid,nodev,noexec,noatime,user_id=1023,group_id=1023,default_permissions,allow_other 0 0
        /dev/fuse /storage/emulated fuse rw,nosuid,nodev,noexec,noatime,user_id=1023,group_id=1023,default_permissions,allow_other 0 0
        /dev/fuse /mnt/runtime/read/emulated fuse rw,nosuid,nodev,noexec,noatime,user_id=1023,group_id=1023,default_permissions,allow_other 0 0
        /dev/fuse /mnt/runtime/write/emulated fuse rw,nosuid,nodev,noexec,noatime,user_id=1023,group_id=1023,default_permissions,allow_other 0 0
        /dev/block/vold/public:179_33 /mnt/media_rw/14DC-DF82 exfat rw,nosuid,nodev,noexec,noatime,uid=1023,gid=1023,fmask=0007,dmask=0007,allow_utime=0020,codepage=cp437,iocharset=utf8,namecase=0,errors=remount-ro 0 0
        /dev/fuse /mnt/runtime/default/14DC-DF82 fuse rw,nosuid,nodev,noexec,noatime,user_id=1023,group_id=1023,default_permissions,allow_other 0 0
        /dev/fuse /storage/14DC-DF82 fuse rw,nosuid,nodev,noexec,noatime,user_id=1023,group_id=1023,default_permissions,allow_other 0 0
        /dev/fuse /mnt/runtime/read/14DC-DF82 fuse rw,nosuid,nodev,noexec,noatime,user_id=1023,group_id=1023,default_permissions,allow_other 0 0
        /dev/fuse /mnt/runtime/write/14DC-DF82 fuse rw,nosuid,nodev,noexec,noatime,user_id=1023,group_id=1023,default_permissions,allow_other 0 0
        /dev/block/vold/public:179_33 /data/media/0/ExtractedApks exfat rw,nosuid,nodev,noexec,noatime,uid=1023,gid=1023,fmask=0007,dmask=0007,allow_utime=0020,codepage=cp437,iocharset=utf8,namecase=0,errors=remount-ro 0 0
        /dev/block/vold/public:179_33 /data/media/0/fastnote exfat rw,nosuid,nodev,noexec,noatime,uid=1023,gid=1023,fmask=0007,dmask=0007,allow_utime=0020,codepage=cp437,iocharset=utf8,namecase=0,errors=remount-ro 0 0
        /dev/block/vold/public:179_33 /data/media/0/Pictures exfat rw,nosuid,nodev,noexec,noatime,uid=1023,gid=1023,fmask=0007,dmask=0007,allow_utime=0020,codepage=cp437,iocharset=utf8,namecase=0,errors=remount-ro 0 0
        /dev/block/vold/public:179_33 /data/media/0/TitaniumBackup exfat rw,nosuid,nodev,noexec,noatime,uid=1023,gid=1023,fmask=0007,dmask=0007,allow_utime=0020,codepage=cp437,iocharset=utf8,namecase=0,errors=remount-ro 0 0
        /dev/block/vold/public:179_33 /data/media/0/Video exfat rw,nosuid,nodev,noexec,noatime,uid=1023,gid=1023,fmask=0007,dmask=0007,allow_utime=0020,codepage=cp437,iocharset=utf8,namecase=0,errors=remount-ro 0 0
*/


/*
        rootfs / rootfs rw 0 0
        proc /proc proc rw,relatime 0 0
        sys /sys sysfs rw,relatime 0 0
        tmpfs / tmpfs ro,relatime 0 0
        /dev/sda1 /mnt ext4 rw,relatime,data=ordered 0 0
        /dev/sda1 /system ext4 rw,relatime,data=ordered 0 0
        tmpfs /cache tmpfs rw,relatime 0 0
        /dev/block/sdb1 /data ext4 rw,relatime,data=ordered 0 0
        tmpfs /dev tmpfs rw,nosuid,relatime,mode=755 0 0
        devpts /dev/pts devpts rw,relatime,mode=600 0 0
        proc /proc proc rw,relatime 0 0
        sysfs /sys sysfs rw,relatime 0 0
        debugfs /sys/kernel/debug debugfs rw,relatime 0 0
        tmpfs /mnt/asec tmpfs rw,relatime,mode=755,gid=1000 0 0
        tmpfs /mnt/obb tmpfs rw,relatime,mode=755,gid=1000 0 0
        /dev/block/sdd1 /storage/extSdCard fuseblk rw,nosuid,nodev,relatime,user_id=0,group_id=0,default_permissions,allow_other,blksize=4096 0 0
        none /proc/sys/fs/binfmt_misc binfmt_misc rw,relatime 0 0
        /dev/block/vold/8:33 /mnt/media_rw/sdcard vfat rw,dirsync,nosuid,nodev,noexec,relatime,uid=1023,gid=1023,fmask=0007,dmask=0007,allow_utime=0020,codepage=437,iocharset=iso8859-1,shortname=mixed,utf8,errors=remount-ro 0 0
        /dev/block/vold/8:33 /mnt/secure/asec vfat rw,dirsync,nosuid,nodev,noexec,relatime,uid=1023,gid=1023,fmask=0007,dmask=0007,allow_utime=0020,codepage=437,iocharset=iso8859-1,shortname=mixed,utf8,errors=remount-ro 0 0
        /dev/fuse /storage/sdcard fuse rw,nosuid,nodev,relatime,user_id=1023,group_id=1023,default_permissions,allow_other 0 0
        InputMapper /storage/sdcard/windows/InputMapper bstfolder rw,nosuid,nodev,relatime,uid=1000,gid=1015,fmask=0702,dmask=0702 0 0
        PublicDocuments /storage/sdcard/windows/PublicDocuments bstfolder rw,nosuid,nodev,relatime,uid=1000,gid=1015,fmask=0702,dmask=0702 0 0
        Documents /storage/sdcard/windows/Documents bstfolder rw,nosuid,nodev,relatime,uid=1000,gid=1015,fmask=0702,dmask=0702 0 0
        Pictures /storage/sdcard/windows/Pictures bstfolder rw,nosuid,nodev,relatime,uid=1000,gid=1015,fmask=0702,dmask=0702 0 0
        BstSharedFolder /storage/sdcard/windows/BstSharedFolder bstfolder rw,nosuid,nodev,relatime,uid=1000,gid=1015,fmask=0702,dmask=0702 0 0
        PublicPictures /storage/sdcard/windows/PublicPictures bstfolder rw,nosuid,nodev,relatime,uid=1000,gid=1015,fmask=0702,dmask=0702 0 0
*/
