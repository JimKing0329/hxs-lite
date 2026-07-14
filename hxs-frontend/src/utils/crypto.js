// 简单的加密密钥（在实际生产环境中应该使用更复杂的密钥）
const ENCRYPTION_KEY = 'hsxf_encryption_key';

// 简单的位移加密函数
const encrypt = (text) => {
    try {
        // 将文本转换为 Base64
        const base64 = btoa(text);
        // 对每个字符进行位移
        const shifted = base64.split('').map(char => {
            const code = char.charCodeAt(0);
            return String.fromCharCode((code + 3) % 65536);
        }).join('');
        return shifted;
    } catch (error) {
        console.error('Encryption error:', error);
        return '';
    }
};

// 解密函数
const decrypt = (encrypted) => {
    try {
        // 反向位移
        const unshifted = encrypted.split('').map(char => {
            const code = char.charCodeAt(0);
            return String.fromCharCode((code - 3 + 65536) % 65536);
        }).join('');
        // 从 Base64 解码
        return atob(unshifted);
    } catch (error) {
        console.error('Decryption error:', error);
        return '';
    }
};

export { encrypt, decrypt }; 