import { post, postToFile, get } from '../tools/axios';

// 模拟接口请求，table示例使用到，会影响导入导出问题，实际项目注释掉
// import 'src/mock/index.js';

const API_PREFIX = '/sendiSystem';

const API = {

    // 登录
    login: params => post(`${API_PREFIX}/login`, params),
    // 登出
    logout: params => post(`${API_PREFIX}/logout`, params),
    // 强制下线
    offline: params => post(`/gateway/oauth2/offline`, params),
    // 验证码图片
    loginImg: (params, headers) => get(`${API_PREFIX}/verifyCode/getVerifyCode`, params, headers),
    // 校验验证码
    loginVCode: params => post(`${API_PREFIX}/verifyCode/judgeCode`, params),
    // 系统信息
    message: params => post(`${API_PREFIX}/sysUser/welcomeInfo`, params),
    // table示例
    tablePage: {
        listByCurrentUser: params => post(`${API_PREFIX}/sysUser/listByCurrentUser`, params),
        // 下拉数据示例接口
        selectList: params => post(`/api/selectList`, params),
        // 树型数据示例接口
        treeSelectList: params => post(`/api/treeSelectList`, params),
        listByRoleId: params => post(`${API_PREFIX}/sysUser/listByRoleId`, params),
        insert: params => post(`${API_PREFIX}/sysUser/insert`, params),
        update: params => post(`${API_PREFIX}/sysUser/update`, params),
        delete: params => post(`${API_PREFIX}/sysUser/delete`, params),
        assignRoles: params => post(`${API_PREFIX}/sysUserRole/assignRoles`, params),
        listRoleIds: params => post(`${API_PREFIX}/sysUserRole/listRoleIds`, params),
        updatePassword: params => post(`${API_PREFIX}/sysUserRole/updatePassword`, params),
        assignUserResources: params =>
            post(`${API_PREFIX}/sysUserRole/assignUserResources`, params),
        list: params => post(`${API_PREFIX}/bmContactsManage/list`, params)
    },
    // 部门管理
    departmentManage: {
        list: params => get(`${API_PREFIX}/sysDepartment/getSubDepartments`, params),
        delete: params =>
            post(
                `${API_PREFIX}/sysDepartment//sendiSystem/sysDepartment/batchDeleteDepartments`,
                params
            ),
        addOrg: params => post(`${API_PREFIX}/sysDepartment/addDepartment`, params),
        editOrg: params => post(`${API_PREFIX}/sysDepartment/updateDepartment`, params)
    },
    // 菜单管理（新）
    menu: {
        listMenuTrees: params => post(`${API_PREFIX}/sysResource/listMenuTrees`, params),
        listMenuAndButtonTrees: params =>
            post(`${API_PREFIX}/sysResource/listMenuAndButtonTrees`, params),
        insert: params => post(`${API_PREFIX}/sysResource/insert`, params),
        update: params => post(`${API_PREFIX}/sysResource/update`, params),
        delete: params => post(`${API_PREFIX}/sysResource/delete`, params),
        // 获取当前用户菜单
        listMenuTreesByCurrentUser: params =>
            post(`${API_PREFIX}/sysResource/listMenuTreesByCurrentUser`, params)
    },
    // 角色管理（新）
    role: {
        listRoleTreesByCurrentUser: params =>
            post(`${API_PREFIX}/sysRole/listRoleTreesByCurrentUser`, params),
        insert: params => post(`${API_PREFIX}/sysRole/insert`, params),
        update: params => post(`${API_PREFIX}/sysRole/update`, params),
        delete: params => post(`${API_PREFIX}/sysRole/delete`, params),
        assignResources: params => post(`${API_PREFIX}/sysRoleResource/assignResources`, params),
        assignResourcesAndButtons: params =>
            post(`${API_PREFIX}/sysRoleResource/assignResourcesAndButtons`, params),
        listMenuTreesByRoleId: params =>
            post(`${API_PREFIX}/sysResource/listMenuTreesByRoleId`, params),
        listMenuAndButtonTreesByRoleId: params =>
            post(`${API_PREFIX}/sysResource/listMenuAndButtonTreesByRoleId`, params),
        // 以下为未使用接口
        queryById: params => post(`${API_PREFIX}/sysRole/queryById`, params)
    },
    // 按钮权限（新）
    sysButton: {
        listButtonTreesByRoleId: params =>
            post(`${API_PREFIX}/sysButton/listButtonTreesByRoleId`, params), // 列出按钮权限管理树
        listRoleAuthButtonTreesByRoleId: params =>
            post(`${API_PREFIX}/sysRoleButton/listRoleAuthButtonTreesByRoleId`, params), // 按钮资源树已被勾选按钮列表
        updateRoleButtonAuth: params =>
            post(`${API_PREFIX}/sysRoleButton/updateRoleButtonAuth`, params), // 更新保存按钮权限管理树
        updateUserButtonAuth: params =>
            post(`${API_PREFIX}/sysUserButton/updateUserButtonAuth`, params), // 更新保存按钮权限管理树
        checkCurrentUserButtonAuthByCode: params =>
            post(`${API_PREFIX}/sysRoleButton/checkCurrentUserButtonAuthByCode`, params), // 检查是否具有按钮权限
        // 以下为未使用接口
        checkExists: params => post(`${API_PREFIX}/sysButton/checkExists`, params), // 检查按钮唯一编码唯一性
        list: params => post(`${API_PREFIX}/sysButton/list`, params),
        insert: params => post(`${API_PREFIX}/sysButton/insert`, params),
        update: params => post(`${API_PREFIX}/sysButton/update`, params),
        delete: params => post(`${API_PREFIX}/sysButton/delete`, params)
    },
    // 用户管理（新）
    userManage: {
        listByCurrentUser: params => post(`${API_PREFIX}/sysUser/listByCurrentUser`, params),
        listByRoleId: params => post(`${API_PREFIX}/sysUser/listByRoleId`, params),
        insert: params => post(`${API_PREFIX}/sysUser/insert`, params),
        update: params => post(`${API_PREFIX}/sysUser/update`, params),
        delete: params => post(`${API_PREFIX}/sysUser/delete`, params),
        // 获取用户已有权限
        listMenuTreesByUserIdFromSysUserResouce: params =>
            post(`${API_PREFIX}/sysResource/listMenuTreesByUserIdFromSysUserResouce`, params),
        listMenuAndButtonTreesByUserId: params =>
            post(`${API_PREFIX}/sysResource/listMenuAndButtonTreesByUserId`, params),

        // 获取当前用户菜单列表
        listMenuTreesByCurrentUser: params =>
            post(`${API_PREFIX}/sysResource/listMenuTreesByCurrentUser`, params),
        // 给指定用户分配菜单权限
        assignResources: params => post(`${API_PREFIX}/sysUserResource/assignResources`, params),
        //给指定用户分配菜单和按钮权限
        assignResourcesAndButtons: params =>
            post(`${API_PREFIX}/sysUserResource/assignResourcesAndButtons`, params),
        // 获取用户可授权按钮权限列表
        listButtonTreesBySysUserIdFromSysUserResouce: params =>
            post(`${API_PREFIX}/sysButton/listButtonTreesBySysUserIdFromSysUserResouce`, params),
        listMenuAndButtonTreesByUserIdFromSysUserResouce: params =>
            post(
                `${API_PREFIX}/sysResource/listMenuAndButtonTreesByUserIdFromSysUserResouce`,
                params
            ),
        // 获取已有按钮权限
        listUserAuthButtonTreesByUserId: params =>
            post(`${API_PREFIX}/sysRoleButton/listUserAuthButtonTreesByUserId`, params),
        assignRoles: params => post(`${API_PREFIX}/sysUserRole/assignRoles`, params),
        listRoleIds: params => post(`${API_PREFIX}/sysUserRole/listRoleIds`, params),
        // 更新密码
        updatePassword: params => post(`${API_PREFIX}/sysUser/updatePassword`, params)
    },

    // 系统参数
    sysConfigPara: {
        list: params => post(`${API_PREFIX}/sysConfigPara/list`, params), // 列表
        insert: params => post(`${API_PREFIX}/sysConfigPara/insert`, params), // 新增
        update: params => post(`${API_PREFIX}/sysConfigPara/update`, params), // 修改
        delete: params => post(`${API_PREFIX}/sysConfigPara/delete`, params), // 删除
        export: params =>
            postToFile(`${API_PREFIX}/sysConfigPara/export`, params, {
                responseType: 'blob'
            }) // 导出
    },
    // 字典(SysDict表)管理
    sysDict: {
        list: params => post(`${API_PREFIX}/sysDict/list`, params), //字典列表
        delete: params => post(`${API_PREFIX}/sysDict/delete`, params), // 删除字典
        insert: params => post(`${API_PREFIX}/sysDict/insert`, params), // 新增字典
        update: params => post(`${API_PREFIX}/sysDict/update`, params), // 更新字典
        export: params =>
            postToFile(`${API_PREFIX}/sysDict/export`, params, {
                responseType: 'blob'
            }), // 字典导出
        listSysDictByDicCode: params => post(`${API_PREFIX}/sysDict/listByDicCode`, params), // 根据dicCode查询字列下拉列表
        listByTableDicCode: params => post(`${API_PREFIX}/sysDict/listByTableDicCode`, params) // 根据dicCode查询字列下拉列表,字典数据在表中
    },
    // 动态API（sys_restapi）
    sysRestApi: {
        list: params => post(`${API_PREFIX}/sysRestapi/list`, params), // 列表
        insert: params => post(`${API_PREFIX}/sysRestapi/insert`, params), // 新增
        update: params => post(`${API_PREFIX}/sysRestapi/update`, params), // 修改
        delete: params => post(`${API_PREFIX}/sysRestapi/delete`, params), // 删除
        export: params =>
            postToFile(`${API_PREFIX}/sysRestapi/export`, params, {
                responseType: 'blob'
            }) // 导出
    },
    // 日志管理
    sysLog: {
        list: params => post(`${API_PREFIX}/sysLogNew/listMachedLogs`, params) // 日志接口
    },
    // 接口管理
    sysInterfaceModule: {
        list: params => post(`${API_PREFIX}/sysInterfaceModule/list`, params), // 列表
        insert: params => post(`${API_PREFIX}/sysInterfaceModule/insert`, params), // 新增
        update: params => post(`${API_PREFIX}/sysInterfaceModule/update`, params), // 修改
        delete: params => post(`${API_PREFIX}/sysInterfaceModule/delete`, params), // 删除
        updateSysInterfaceModule: params =>
            post(`${API_PREFIX}/sysInterfaceModule/updateSysInterfaceModule`, params) // 日志模块映射翻译更新接口
    },
    // 数据权限-角色授权
    sysRoleDatapermissonRule: {
        listBySysDatapermissonRuleId: params =>
            post(`${API_PREFIX}/sysRoleDatapermissonRule/listBySysDatapermissonRuleId`, params), // 已授权角色列表
        listUnGrantsBySysDatapermissonRuleId: params =>
            post(
                `${API_PREFIX}/sysRoleDatapermissonRule/listUnGrantsBySysDatapermissonRuleId`,
                params
            ), // 未授权角色列表
        insert: params =>
            post(`${API_PREFIX}/sysRoleDatapermissonRule/insertSysRoleDatapermissonRules`, params), // 增加权限
        delete: params =>
            post(`${API_PREFIX}/sysRoleDatapermissonRule/deleteSysRoleDatapermissonRules`, params) // 回收权限
    },
    // 数据权限-用户授权
    sysUserDatapermissonRule: {
        listBySysDatapermissonRuleId: params =>
            post(`${API_PREFIX}/sysUserDatapermissonRule/listBySysDatapermissonRuleId`, params), // 已授权用户列表
        listUnGrantsBySysDatapermissonRuleId: params =>
            post(
                `${API_PREFIX}/sysUserDatapermissonRule/listUnGrantsBySysDatapermissonRuleId`,
                params
            ), // 未授权用户列表
        insert: params =>
            post(`${API_PREFIX}/sysUserDatapermissonRule/insertSysUserDatapermissonRules`, params), // 增加权限
        delete: params =>
            post(`${API_PREFIX}/sysUserDatapermissonRule/deleteSysUserDatapermissonRules`, params) // 回收权限
    },

    // 数据权限管理
    sysDatapermissonRule: {
        list: params => post(`${API_PREFIX}/sysDatapermissonRule/list`, params), // 列表
        insert: params => post(`${API_PREFIX}/sysDatapermissonRule/insert`, params), // 新增
        update: params => post(`${API_PREFIX}/sysDatapermissonRule/update`, params), // 修改
        delete: params => post(`${API_PREFIX}/sysDatapermissonRule/delete`, params), // 删除
        export: params =>
            postToFile(`${API_PREFIX}/sysDatapermissonRule/export`, params, {
                responseType: 'blob'
            }), // 导出
        // 以下为未使用接口
        listRuleTreesByRoleId: params =>
            post(`${API_PREFIX}/sysRule/listRuleTreesByRoleId`, params), // 数据权限菜单树
        listRoleAuthRuleTreesByRoleId: params =>
            post(`${API_PREFIX}/sysRoleRule/listRoleAuthRuleTreesByRoleId`, params) // 当前角色菜单树
    },

    // 接口权限授权管理
    sysRoleInterfaceModule: {
        listByRoleId: params => post(`${API_PREFIX}/sysRoleInterfaceModule/listByRoleId`, params), // 已授权了的列表
        listUnGrantsByRoleId: params =>
            post(`${API_PREFIX}/sysRoleInterfaceModule/listUnGrantsByRoleId`, params), // 未授权的列表
        insertSysRoleInterfaceModules: params =>
            post(`${API_PREFIX}/sysRoleInterfaceModule/insertSysRoleInterfaceModules`, params), // 增加权限
        deleteSysRoleInterfaceModules: params =>
            post(`${API_PREFIX}/sysRoleInterfaceModule/deleteSysRoleInterfaceModules`, params) // 回收权限
    },
    // 系统使用情况
    systemUsage: {
        welcomeInfo: params => post(`${API_PREFIX}/sysUser/welcomeInfo`, params), // 获取在线人数
        listUserNumInfo: params => post(`${API_PREFIX}/sysLogNew/listUserNumInfo`, params), // 获取访问量
        onlineTrend: params => post(`${API_PREFIX}/sysLogNew/onlineTrend`, params), // 获取用户登录趋势
        listErrorCode: params => post(`${API_PREFIX}/sysLogNew/listErrorCode`, params), // 获取用户异常数 按错误码
        listErrorModule: params => post(`${API_PREFIX}/sysLogNew/listErrorModule`, params), // 获取用户异常数 按模块
        listSubmodule: params => post(`${API_PREFIX}/sysLogNew/listSubmodule`, params), // 获取子模块访问量
        listModule: params => post(`${API_PREFIX}/sysLogNew/listModule`, params), // 获取模块访问量
        queryTopActiveCount: params => post(`${API_PREFIX}/sysLogNew/queryTopActiveCount`, params) // 获取用户地区活跃排名
    },
    // 未使用接口
    // 删除文件接口
    deleteFile: commonFileId =>
        post(`${API_PREFIX}/commonFile/deleteCommonFile?commonFileId=${commonFileId}`), // 删除
    // 查看图片接口
    getImage: params =>
        `${API_PREFIX}/commonFile/getImage?filePath=${params}&token=${localStorage.getItem(
            'token'
        )}`

};

export default API;
