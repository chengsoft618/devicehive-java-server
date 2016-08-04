package com.devicehive.dao.riak;


import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.datatypes.CounterUpdate;
import com.basho.riak.client.api.commands.datatypes.FetchCounter;
import com.basho.riak.client.api.commands.datatypes.UpdateCounter;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.devicehive.dao.AccessKeyDao;
import com.devicehive.dao.AccessKeyPermissionDao;
import com.devicehive.model.AccessKey;
import com.devicehive.model.AccessKeyPermission;
import com.devicehive.vo.AccessKeyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Profile({"riak"})
@Repository
public class AccessKeyPermissionDaoRiakImpl extends RiakGenericDao implements AccessKeyPermissionDao {

    private static final Namespace COUNTER_NS = new Namespace("counters", "access_key_permission_counters");

    @Autowired
    private AccessKeyDao accessKeyDao;

    @Override
    public int deleteByAccessKey(AccessKeyVO key) {
        if (key.getPermissions() != null) {
            int result = key.getPermissions().size();
            key.getPermissions().clear();
            accessKeyDao.merge(key);
            return result;
        }
        return 0;
    }

    @Override
    public void persist(AccessKeyVO key, AccessKeyPermission accessKeyPermission) {
        if (accessKeyPermission.getAccessKey() == null) {
            accessKeyPermission.setAccessKey(AccessKey.convert(key));
        }
        merge(accessKeyPermission);
    }

    @Override
    public AccessKeyPermission merge(AccessKeyPermission accessKeyPermission) {
        AccessKeyVO accessKey = AccessKey.convert(accessKeyPermission.getAccessKey());
        accessKey = accessKeyDao.find(accessKey.getId());

        if (accessKeyPermission.getId() == null) {
            Location accessKeyPermissionCounters = new Location(COUNTER_NS, "access_key_permission_counter");
            accessKeyPermission.setId(getId(accessKeyPermissionCounters));
        }

        if (accessKey!= null && accessKey.getPermissions() != null) {
            Set<AccessKeyPermission> permissions = accessKey.getPermissions();
            permissions.add(accessKeyPermission);

            Iterator<AccessKeyPermission> iterator = permissions.iterator();
            while (iterator.hasNext()) {
                AccessKeyPermission element = iterator.next();
                if (element.getId() == null) {
                    iterator.remove();
                }
            }
        } else {
            Set<AccessKeyPermission> accessKeyPermissions = new HashSet<>();
            accessKeyPermissions.add(accessKeyPermission);
            accessKey.setPermissions(accessKeyPermissions);
        }

        accessKeyDao.merge(accessKey);

        return accessKeyPermission;
    }
}
