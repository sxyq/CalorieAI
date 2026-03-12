#!/usr/bin/env python3
"""
CalorieAI 图标生成脚本
将原始图标转换为Android应用所需的各种尺寸
"""

from PIL import Image
import os
import sys

# 图标尺寸配置 (宽, 高)
ICON_SIZES = {
    'mipmap-mdpi': (48, 48),
    'mipmap-hdpi': (72, 72),
    'mipmap-xhdpi': (96, 96),
    'mipmap-xxhdpi': (144, 144),
    'mipmap-xxxhdpi': (192, 192),
}

# 前台图标尺寸 (自适应图标)
FOREGROUND_SIZES = {
    'mipmap-mdpi': (108, 108),
    'mipmap-hdpi': (162, 162),
    'mipmap-xhdpi': (216, 216),
    'mipmap-xxhdpi': (324, 324),
    'mipmap-xxxhdpi': (432, 432),
}

def create_directories(base_path):
    """创建必要的目录结构"""
    for folder in ICON_SIZES.keys():
        path = os.path.join(base_path, 'app', 'src', 'main', 'res', folder)
        os.makedirs(path, exist_ok=True)
        print(f"✓ 创建目录: {path}")

def resize_image(input_path, output_path, size, fill_background=True):
    """
    调整图片尺寸
    :param input_path: 输入图片路径
    :param output_path: 输出图片路径
    :param size: 目标尺寸 (宽, 高)
    :param fill_background: 是否填充白色背景
    """
    try:
        with Image.open(input_path) as img:
            # 转换为RGBA模式
            img = img.convert('RGBA')
            
            # 创建白色背景
            if fill_background:
                background = Image.new('RGBA', size, (255, 255, 255, 255))
            else:
                background = Image.new('RGBA', size, (0, 0, 0, 0))
            
            # 计算缩放比例，保持宽高比
            img.thumbnail(size, Image.Resampling.LANCZOS)
            
            # 计算居中位置
            x = (size[0] - img.width) // 2
            y = (size[1] - img.height) // 2
            
            # 粘贴图片到背景
            background.paste(img, (x, y), img)
            
            # 保存为PNG
            background.save(output_path, 'PNG')
            print(f"✓ 生成图标: {output_path} ({size[0]}x{size[1]})")
            return True
            
    except Exception as e:
        print(f"✗ 错误: {e}")
        return False

def generate_icons(source_image_path):
    """生成所有尺寸的图标"""
    
    # 获取项目路径
    script_dir = os.path.dirname(os.path.abspath(__file__))
    res_path = os.path.join(script_dir, 'app', 'src', 'main', 'res')
    
    print("=" * 60)
    print("CalorieAI 图标生成工具")
    print("=" * 60)
    print(f"\n源文件: {source_image_path}")
    print(f"输出目录: {res_path}\n")
    
    # 检查源文件是否存在
    if not os.path.exists(source_image_path):
        print(f"✗ 错误: 找不到源文件 {source_image_path}")
        print("\n请确保图标文件路径正确")
        return False
    
    # 创建目录
    create_directories(script_dir)
    
    success_count = 0
    
    # 生成前台图标 (ic_launcher_foreground.png)
    print("\n--- 生成前台图标 ---")
    for folder, size in FOREGROUND_SIZES.items():
        output_path = os.path.join(res_path, folder, 'ic_launcher_foreground.png')
        if resize_image(source_image_path, output_path, size, fill_background=False):
            success_count += 1
    
    # 生成圆形图标 (ic_launcher_round.png) - 使用相同尺寸
    print("\n--- 生成圆形图标 ---")
    for folder, size in ICON_SIZES.items():
        output_path = os.path.join(res_path, folder, 'ic_launcher_round.png')
        if resize_image(source_image_path, output_path, size, fill_background=True):
            success_count += 1
    
    # 生成方形图标 (ic_launcher.png) - 使用相同尺寸
    print("\n--- 生成方形图标 ---")
    for folder, size in ICON_SIZES.items():
        output_path = os.path.join(res_path, folder, 'ic_launcher.png')
        if resize_image(source_image_path, output_path, size, fill_background=True):
            success_count += 1
    
    print("\n" + "=" * 60)
    print(f"✓ 图标生成完成! 共生成 {success_count} 个文件")
    print("=" * 60)
    
    return True

def main():
    """主函数"""
    # 默认图标路径
    default_icon = "ChatGPT Image 2026年3月11日 19_09_33.png"
    
    # 获取命令行参数或使用默认路径
    if len(sys.argv) > 1:
        source_path = sys.argv[1]
    else:
        # 尝试在当前目录和父目录查找
        script_dir = os.path.dirname(os.path.abspath(__file__))
        parent_dir = os.path.dirname(script_dir)
        
        possible_paths = [
            os.path.join(script_dir, default_icon),
            os.path.join(parent_dir, default_icon),
            default_icon,
        ]
        
        source_path = None
        for path in possible_paths:
            if os.path.exists(path):
                source_path = path
                break
        
        if not source_path:
            print("✗ 错误: 找不到图标文件")
            print(f"\n请提供图标文件路径，例如:")
            print(f"  python generate_icons.py \"path/to/your/icon.png\"")
            print(f"\n或确保 '{default_icon}' 在项目目录中")
            return
    
    # 生成图标
    if generate_icons(source_path):
        print("\n✓ 所有图标已生成到 app/src/main/res/mipmap-*/ 目录")
        print("\n现在可以使用 Android Studio 构建 APK 了!")
    else:
        print("\n✗ 图标生成失败，请检查错误信息")

if __name__ == "__main__":
    main()
