package com.heroesports.heroci.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import com.heroesports.heroci.data.entity.CheckIn
import com.heroesports.heroci.data.entity.Member
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ExportUtils {
    private const val TAG = "ExportUtils"

    fun exportToExcel(
        context: Context,
        members: List<Member>,
        checkIns: List<CheckIn>,
        startDate: LocalDate,
        endDate: LocalDate,
        outputFile: File
    ): Boolean {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("打卡记录")
            
            // 创建标题行样式
            val headerStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                alignment = HorizontalAlignment.CENTER
            }

            // 创建标题行
            val headerRow = sheet.createRow(0)
            val headers = listOf("姓名", "日期", "打卡时间", "位置", "照片")
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).apply {
                    setCellValue(header)
                    cellStyle = headerStyle
                }
            }

            // 设置列宽
            sheet.setColumnWidth(0, (15 * 256).toInt())  // 姓名
            sheet.setColumnWidth(1, (12 * 256).toInt())  // 日期
            sheet.setColumnWidth(2, (10 * 256).toInt())  // 打卡时间
            sheet.setColumnWidth(3, (40 * 256).toInt())  // 位置
            sheet.setColumnWidth(4, (50 * 256).toInt())  // 照片

            // 创建日期格式
            val dateStyle = workbook.createCellStyle().apply {
                dataFormat = workbook.createDataFormat().getFormat("yyyy-MM-dd")
            }
            val timeStyle = workbook.createCellStyle().apply {
                dataFormat = workbook.createDataFormat().getFormat("HH:mm:ss")
            }

            var rowNum = 1
            var currentDate = startDate
            while (!currentDate.isAfter(endDate)) {
                for (member in members) {
                    // 获取当天的打卡记录
                    val dayCheckIn = checkIns.find { checkIn ->
                        checkIn.memberName == member.name &&
                        checkIn.checkInTime.toLocalDate() == currentDate
                    }

                    val row = sheet.createRow(rowNum++)
                    row.createCell(0).setCellValue(member.name)
                    
                    // 设置日期
                    row.createCell(1).apply {
                        setCellValue(java.sql.Date.valueOf(currentDate.toString()))
                        cellStyle = dateStyle
                    }

                    if (dayCheckIn != null) {
                        // 设置打卡时间
                        row.createCell(2).apply {
                            setCellValue(dayCheckIn.checkInTime.format(
                                DateTimeFormatter.ofPattern("HH:mm:ss")
                            ))
                        }
                        row.createCell(3).setCellValue(dayCheckIn.location)

                        // 处理照片
                        try {
                            val photoUri = Uri.parse(dayCheckIn.photoPath)
                            val imageBytes = context.contentResolver.openInputStream(photoUri)?.use { input ->
                                // 读取图片并处理方向
                                val bitmap = BitmapFactory.decodeStream(input)
                                val rotatedBitmap = rotateImageIfRequired(context, bitmap, photoUri)
                                
                                // 压缩图片
                                val baos = ByteArrayOutputStream()
                                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                                rotatedBitmap.recycle()
                                baos.toByteArray()
                            }

                            if (imageBytes != null) {
                                // 添加图片到工作簿
                                val pictureIdx = workbook.addPicture(
                                    imageBytes,
                                    Workbook.PICTURE_TYPE_JPEG
                                )

                                // 创建图片锚点
                                val helper: CreationHelper = workbook.creationHelper
                                val drawing: Drawing<*> = sheet.createDrawingPatriarch()
                                val anchor: ClientAnchor = helper.createClientAnchor()

                                // 设置图片位置和大小
                                anchor.setCol1(4) // 第5列
                                anchor.setRow1(rowNum - 1) // 当前行
                                anchor.setCol2(5) // 结束列
                                anchor.setRow2(rowNum) // 结束行
                                anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE) // 允许调整大小

                                // 设置行高以保持图片比例
                                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
                                val rowHeight = (150 * 20).toShort() // 基础高度
                                row.height = rowHeight
                                
                                // 设置列宽以匹配图片比例
                                val columnWidth = (rowHeight / 20 * aspectRatio * 256 / 6).toInt()
                                sheet.setColumnWidth(4, columnWidth.coerceAtMost(15000)) // 限制最大宽度

                                // 插入图片
                                drawing.createPicture(anchor, pictureIdx)
                                
                                bitmap.recycle()
                            } else {
                                row.createCell(4).setCellValue("无法加载照片")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "处理照片失败: ${e.message}", e)
                            row.createCell(4).setCellValue("无法加载照片")
                        }
                    } else {
                        row.createCell(2).setCellValue("未打卡")
                        row.createCell(3).setCellValue("-")
                        row.createCell(4).setCellValue("-")
                    }
                }
                currentDate = currentDate.plusDays(1)
            }

            // 保存文件
            try {
                FileOutputStream(outputFile).use { fileOut ->
                    workbook.write(fileOut)
                    fileOut.flush()
                }
                Log.d(TAG, "Excel文件已保存: ${outputFile.absolutePath}, 大小: ${outputFile.length()}")
                true
            } catch (e: Exception) {
                Log.e(TAG, "保存Excel文件失败: ${e.message}", e)
                false
            } finally {
                workbook.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "导出Excel失败: ${e.message}", e)
            false
        }
    }

    private fun rotateImageIfRequired(context: Context, bitmap: Bitmap, photoUri: Uri): Bitmap {
        try {
            context.contentResolver.openInputStream(photoUri)?.use { input ->
                val exif = ExifInterface(input)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )

                val matrix = Matrix()
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    else -> return bitmap
                }

                return Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.width,
                    bitmap.height,
                    matrix,
                    true
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "读取EXIF信息失败: ${e.message}", e)
        }
        return bitmap
    }
} 