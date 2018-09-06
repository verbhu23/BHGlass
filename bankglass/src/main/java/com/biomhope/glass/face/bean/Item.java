package com.biomhope.glass.face.bean;

import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

/**
 * Created by liuhui on 2017/9/13.
 */

@Table("gen_feature_iddatabase")
public class Item {
    @PrimaryKey(AssignType.AUTO_INCREMENT)
    @Column("id")
    public int id;
    @Column("md5")
    public String md5;
    @Column("feature")
    public byte[] feature;
    @Column("name")
    public String name;
    @Column("IDcard")
    public String IDcard;
    @Column("mobile")
    public String mobile;

    @Column("img_dir")
    public String img_dir;

    @Column("img_blob")
    public byte[] img_blob;

    public Item() {
    }
}
