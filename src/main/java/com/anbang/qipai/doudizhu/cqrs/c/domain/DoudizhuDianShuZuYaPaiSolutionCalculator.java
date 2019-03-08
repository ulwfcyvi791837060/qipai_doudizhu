package com.anbang.qipai.doudizhu.cqrs.c.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dml.doudizhu.pai.dianshuzu.FeijidaierDianShuZu;
import com.dml.doudizhu.pai.dianshuzu.FeijidaiyiDianShuZu;
import com.dml.doudizhu.pai.dianshuzu.SandaierDianShuZu;
import com.dml.doudizhu.pai.dianshuzu.SandaiyiDianShuZu;
import com.dml.doudizhu.pai.dianshuzu.SidaierDianShuZu;
import com.dml.doudizhu.pai.dianshuzu.comparator.ChibangDianShuZuComparator;
import com.dml.doudizhu.pai.dianshuzu.comparator.FeijiDianShuZuComparator;
import com.dml.doudizhu.player.action.da.DianShuZuYaPaiSolutionCalculator;
import com.dml.doudizhu.player.action.da.solution.DaPaiDianShuSolution;
import com.dml.puke.wanfa.dianshu.dianshuzu.DanzhangDianShuZu;
import com.dml.puke.wanfa.dianshu.dianshuzu.DianShuZu;
import com.dml.puke.wanfa.dianshu.dianshuzu.DianShuZuGenerator;
import com.dml.puke.wanfa.dianshu.dianshuzu.DuiziDianShuZu;
import com.dml.puke.wanfa.dianshu.dianshuzu.LianduiDianShuZu;
import com.dml.puke.wanfa.dianshu.dianshuzu.LiansanzhangDianShuZu;
import com.dml.puke.wanfa.dianshu.dianshuzu.SanzhangDianShuZu;
import com.dml.puke.wanfa.dianshu.dianshuzu.ShunziDianShuZu;
import com.dml.puke.wanfa.dianshu.dianshuzu.comparator.CanNotCompareException;
import com.dml.puke.wanfa.dianshu.dianshuzu.comparator.DanGeDianShuZuComparator;
import com.dml.puke.wanfa.dianshu.dianshuzu.comparator.LianXuDianShuZuComparator;

/**
 * 斗地主点数压牌
 * 
 * @author lsc
 *
 */
public class DoudizhuDianShuZuYaPaiSolutionCalculator implements DianShuZuYaPaiSolutionCalculator {

	private DanGeDianShuZuComparator danGeDianShuZuComparator;

	private LianXuDianShuZuComparator lianXuDianShuZuComparator;

	private ChibangDianShuZuComparator chibangDianShuZuComparator;

	private FeijiDianShuZuComparator feijiDianShuZuComparator;

	@Override
	public Map<String, DaPaiDianShuSolution> calculate(DianShuZu beiYaDianShuZu, int[] dianShuAmountArray) {
		Map<String, DaPaiDianShuSolution> kedaPaiSolutions = new HashMap<>();

		if (beiYaDianShuZu instanceof DanzhangDianShuZu) {// 单张
			DanzhangDianShuZu beiYaDanzhangDianShuZu = (DanzhangDianShuZu) beiYaDianShuZu;
			List<DanzhangDianShuZu> danzhangList = DianShuZuGenerator.generateAllDanzhangDianShuZu(dianShuAmountArray);
			List<DanzhangDianShuZu> danzhangFilterList = new ArrayList<>();
			for (DanzhangDianShuZu danzhangDianShuZu : danzhangList) {
				try {
					if (danGeDianShuZuComparator.compare(danzhangDianShuZu, beiYaDanzhangDianShuZu) > 0) {
						danzhangFilterList.add(danzhangDianShuZu);
					}
				} catch (CanNotCompareException e) {
					e.printStackTrace();
				}
			}
			List<DaPaiDianShuSolution> danzhangSolution = DoudizhuSolutionGenerator
					.calculateDanzhangDaPaiDianShuSolution(danzhangFilterList);
			danzhangSolution.forEach((solution) -> {
				kedaPaiSolutions.put(solution.getDianshuZuheIdx(), solution);
			});
		} else if (beiYaDianShuZu instanceof DuiziDianShuZu) {// 对子
			DuiziDianShuZu beiYaDuiziDianShuZu = (DuiziDianShuZu) beiYaDianShuZu;
			List<DuiziDianShuZu> duiziList = DianShuZuGenerator.generateAllDuiziDianShuZu(dianShuAmountArray);
			List<DuiziDianShuZu> duiziFilterList = new ArrayList<>();
			for (DuiziDianShuZu duiziDianShuZu : duiziList) {
				try {
					if (danGeDianShuZuComparator.compare(duiziDianShuZu, beiYaDuiziDianShuZu) > 0) {
						duiziFilterList.add(duiziDianShuZu);
					}
				} catch (CanNotCompareException e) {
					e.printStackTrace();
				}
			}
			List<DaPaiDianShuSolution> duiziSolution = DoudizhuSolutionGenerator
					.calculateDuiziDaPaiDianShuSolution(duiziFilterList);
			duiziSolution.forEach((solution) -> {
				kedaPaiSolutions.put(solution.getDianshuZuheIdx(), solution);
			});
		} else if (beiYaDianShuZu instanceof SanzhangDianShuZu) {// 三张
			SanzhangDianShuZu beiYaSanzhangDianShuZu = (SanzhangDianShuZu) beiYaDianShuZu;
			List<SanzhangDianShuZu> sanzhangList = DianShuZuGenerator.generateAllSanzhangDianShuZu(dianShuAmountArray);
			List<SanzhangDianShuZu> sanzhangFilterList = new ArrayList<>();
			for (SanzhangDianShuZu sanzhangDianShuZu : sanzhangList) {
				try {
					if (danGeDianShuZuComparator.compare(sanzhangDianShuZu, beiYaSanzhangDianShuZu) > 0) {
						sanzhangFilterList.add(sanzhangDianShuZu);
					}
				} catch (CanNotCompareException e) {
					e.printStackTrace();
				}
			}
			List<DaPaiDianShuSolution> sanzhangSolution = DoudizhuSolutionGenerator
					.calculateSanzhangDaPaiDianShuSolution(sanzhangFilterList);
			sanzhangSolution.forEach((solution) -> {
				kedaPaiSolutions.put(solution.getDianshuZuheIdx(), solution);
			});
		} else if (beiYaDianShuZu instanceof ShunziDianShuZu) {// 顺子
			ShunziDianShuZu beiYaShunziDianShuZu = (ShunziDianShuZu) beiYaDianShuZu;
			List<ShunziDianShuZu> shunziList = new ArrayList<>();
			for (int length = 5; length < 12; length++) {// 顺子:五张或更多的连续单牌,不包括2与双王,如单5+单6+单7+单8+单9.
				shunziList.addAll(DianShuZuGenerator.generateAllShunziDianShuZu(dianShuAmountArray, length));
			}
			List<ShunziDianShuZu> shunziFilterList = new ArrayList<>();
			for (ShunziDianShuZu shunziDianShuZu : shunziList) {
				try {
					if (lianXuDianShuZuComparator.compare(shunziDianShuZu, beiYaShunziDianShuZu) > 0) {
						shunziFilterList.add(shunziDianShuZu);
					}
				} catch (CanNotCompareException e) {
					e.printStackTrace();
				}
			}
			List<DaPaiDianShuSolution> shunziSolution = DoudizhuSolutionGenerator
					.calculateShunziDaPaiDianShuSolution(shunziFilterList);
			shunziSolution.forEach((solution) -> {
				kedaPaiSolutions.put(solution.getDianshuZuheIdx(), solution);
			});
		} else if (beiYaDianShuZu instanceof LianduiDianShuZu) {// 连对
			LianduiDianShuZu beiYaLianduiDianShuZu = (LianduiDianShuZu) beiYaDianShuZu;
			List<LianduiDianShuZu> lianduiList = new ArrayList<>();
			for (int length = 3; length < 12; length++) {// 连对:三对或更多的连续对子,不包括2与双王,如对5+对6+对7.
				lianduiList.addAll(DianShuZuGenerator.generateAllLianduiDianShuZu(dianShuAmountArray, length));
			}
			List<LianduiDianShuZu> lianduiFilterList = new ArrayList<>();
			for (LianduiDianShuZu lianduiDianShuZu : lianduiList) {
				try {
					if (lianXuDianShuZuComparator.compare(lianduiDianShuZu, beiYaLianduiDianShuZu) > 0) {
						lianduiFilterList.add(lianduiDianShuZu);
					}
				} catch (CanNotCompareException e) {
					e.printStackTrace();
				}
			}
			List<DaPaiDianShuSolution> lianduiSolution = DoudizhuSolutionGenerator
					.calculateLianduiDaPaiDianShuSolution(lianduiFilterList);
			lianduiSolution.forEach((solution) -> {
				kedaPaiSolutions.put(solution.getDianshuZuheIdx(), solution);
			});
		} else if (beiYaDianShuZu instanceof LiansanzhangDianShuZu) {// 连三张
			LiansanzhangDianShuZu beiYaLiansanzhangDianShuZu = (LiansanzhangDianShuZu) beiYaDianShuZu;
			List<LiansanzhangDianShuZu> lianSanZhangList = new ArrayList<>();
			for (int length = 2; length < 12; length++) {// 连三张:二或更多的连续三张,不包括2与双王
				lianSanZhangList
						.addAll(DianShuZuGenerator.generateAllLiansanzhangDianShuZu(dianShuAmountArray, length));
			}
			List<LiansanzhangDianShuZu> lianSanZhangFilterList = new ArrayList<>();
			for (LiansanzhangDianShuZu liansanzhangDianShuZu : lianSanZhangList) {
				try {
					if (lianXuDianShuZuComparator.compare(liansanzhangDianShuZu, beiYaLiansanzhangDianShuZu) > 0) {
						lianSanZhangFilterList.add(liansanzhangDianShuZu);
					}
				} catch (CanNotCompareException e) {
					e.printStackTrace();
				}
			}
			List<DaPaiDianShuSolution> liansanzhangSolution = DoudizhuSolutionGenerator
					.calculateLiansanzhangDaPaiDianShuSolution(lianSanZhangFilterList);
			liansanzhangSolution.forEach((solution) -> {
				kedaPaiSolutions.put(solution.getDianshuZuheIdx(), solution);
			});
		} else if (beiYaDianShuZu instanceof SandaiyiDianShuZu) {// 三带一
			SandaiyiDianShuZu beiYaSandaiyiDianShuZu = (SandaiyiDianShuZu) beiYaDianShuZu;
			List<SandaiyiDianShuZu> sandaiyiList = DoudizhuSolutionGenerator
					.generateAllSandaiyiDianShuZu(dianShuAmountArray);
			List<SandaiyiDianShuZu> sandaiyiFilterList = new ArrayList<>();
			for (SandaiyiDianShuZu sandaiyiDianShuZu : sandaiyiList) {
				try {
					if (chibangDianShuZuComparator.compare(sandaiyiDianShuZu, beiYaSandaiyiDianShuZu) > 0) {
						sandaiyiFilterList.add(sandaiyiDianShuZu);
					}
				} catch (CanNotCompareException e) {
					e.printStackTrace();
				}
			}
			List<DaPaiDianShuSolution> sandaiyiSolution = DoudizhuSolutionGenerator
					.calculateSandaiyiDaPaiDianShuSolution(sandaiyiFilterList);
			sandaiyiSolution.forEach((solution) -> {
				kedaPaiSolutions.put(solution.getDianshuZuheIdx(), solution);
			});
		} else if (beiYaDianShuZu instanceof SandaierDianShuZu) {// 三带二
			SandaierDianShuZu beiYaSandaierDianShuZu = (SandaierDianShuZu) beiYaDianShuZu;
			List<SandaierDianShuZu> sandaierList = DoudizhuSolutionGenerator
					.generateAllSandaierDianShuZu(dianShuAmountArray);
			List<SandaierDianShuZu> sandaierFilterList = new ArrayList<>();
			for (SandaierDianShuZu sandaierDianShuZu : sandaierList) {
				try {
					if (chibangDianShuZuComparator.compare(sandaierDianShuZu, beiYaSandaierDianShuZu) > 0) {
						sandaierFilterList.add(sandaierDianShuZu);
					}
				} catch (CanNotCompareException e) {
					e.printStackTrace();
				}
			}
			List<DaPaiDianShuSolution> sandaierSolution = DoudizhuSolutionGenerator
					.calculateSandaierDaPaiDianShuSolution(sandaierFilterList);
			sandaierSolution.forEach((solution) -> {
				kedaPaiSolutions.put(solution.getDianshuZuheIdx(), solution);
			});
		} else if (beiYaDianShuZu instanceof SidaierDianShuZu) {// 四带二
			SidaierDianShuZu beiYaSidaierDianShuZu = (SidaierDianShuZu) beiYaDianShuZu;
			List<SidaierDianShuZu> sidaierList = DoudizhuSolutionGenerator
					.generateAllSidaierDianShuZu(dianShuAmountArray);
			List<SidaierDianShuZu> sidaierFilterList = new ArrayList<>();
			for (SidaierDianShuZu sidaierDianShuZu : sidaierList) {
				try {
					if (chibangDianShuZuComparator.compare(sidaierDianShuZu, beiYaSidaierDianShuZu) > 0) {
						sidaierFilterList.add(sidaierDianShuZu);
					}
				} catch (CanNotCompareException e) {
					e.printStackTrace();
				}
			}
			List<DaPaiDianShuSolution> sidaierSolution = DoudizhuSolutionGenerator
					.calculateSidaierDaPaiDianShuSolution(sidaierFilterList);
			sidaierSolution.forEach((solution) -> {
				kedaPaiSolutions.put(solution.getDianshuZuheIdx(), solution);
			});
		} else if (beiYaDianShuZu instanceof FeijidaiyiDianShuZu) {// 飞机带单张
			FeijidaiyiDianShuZu beiYaFeijidaiyiDianShuZu = (FeijidaiyiDianShuZu) beiYaDianShuZu;
			List<FeijidaiyiDianShuZu> feijidaiyiList = new ArrayList<>();
			for (int length = 2; length < 12; length++) {// 连三张:二或更多的连续三张,不包括2与双王
				feijidaiyiList
						.addAll(DoudizhuSolutionGenerator.generateAllFeijidaiyiDianShuZu(dianShuAmountArray, length));
			}
			List<FeijidaiyiDianShuZu> feijidaiyiFilterList = new ArrayList<>();
			for (FeijidaiyiDianShuZu feijidaiyiDianShuZu : feijidaiyiList) {
				try {
					if (feijiDianShuZuComparator.compare(feijidaiyiDianShuZu, beiYaFeijidaiyiDianShuZu) > 0) {
						feijidaiyiFilterList.add(feijidaiyiDianShuZu);
					}
				} catch (CanNotCompareException e) {
					e.printStackTrace();
				}
			}
			List<DaPaiDianShuSolution> feijidaiyiSolution = DoudizhuSolutionGenerator
					.calculateFeijidaiyiDaPaiDianShuSolution(feijidaiyiFilterList);
			feijidaiyiSolution.forEach((solution) -> {
				kedaPaiSolutions.put(solution.getDianshuZuheIdx(), solution);
			});
		} else if (beiYaDianShuZu instanceof FeijidaierDianShuZu) {// 飞机带对子
			FeijidaierDianShuZu beiYaFeijidaierDianShuZu = (FeijidaierDianShuZu) beiYaDianShuZu;
			List<FeijidaierDianShuZu> feijidaierList = new ArrayList<>();
			for (int length = 2; length < 12; length++) {// 连三张:二或更多的连续三张,不包括2与双王
				feijidaierList
						.addAll(DoudizhuSolutionGenerator.generateAllFeijidaierDianShuZu(dianShuAmountArray, length));
			}
			List<FeijidaierDianShuZu> feijidaierFilterList = new ArrayList<>();
			for (FeijidaierDianShuZu feijidaierDianShuZu : feijidaierList) {
				try {
					if (feijiDianShuZuComparator.compare(feijidaierDianShuZu, beiYaFeijidaierDianShuZu) > 0) {
						feijidaierFilterList.add(feijidaierDianShuZu);
					}
				} catch (CanNotCompareException e) {
					e.printStackTrace();
				}
			}
			List<DaPaiDianShuSolution> feijidaierSolution = DoudizhuSolutionGenerator
					.calculateFeijidaierDaPaiDianShuSolution(feijidaierFilterList);
			feijidaierSolution.forEach((solution) -> {
				kedaPaiSolutions.put(solution.getDianshuZuheIdx(), solution);
			});
		}
		return kedaPaiSolutions;
	}

	public DanGeDianShuZuComparator getDanGeDianShuZuComparator() {
		return danGeDianShuZuComparator;
	}

	public void setDanGeDianShuZuComparator(DanGeDianShuZuComparator danGeDianShuZuComparator) {
		this.danGeDianShuZuComparator = danGeDianShuZuComparator;
	}

	public LianXuDianShuZuComparator getLianXuDianShuZuComparator() {
		return lianXuDianShuZuComparator;
	}

	public void setLianXuDianShuZuComparator(LianXuDianShuZuComparator lianXuDianShuZuComparator) {
		this.lianXuDianShuZuComparator = lianXuDianShuZuComparator;
	}

	public ChibangDianShuZuComparator getChibangDianShuZuComparator() {
		return chibangDianShuZuComparator;
	}

	public void setChibangDianShuZuComparator(ChibangDianShuZuComparator chibangDianShuZuComparator) {
		this.chibangDianShuZuComparator = chibangDianShuZuComparator;
	}

	public FeijiDianShuZuComparator getFeijiDianShuZuComparator() {
		return feijiDianShuZuComparator;
	}

	public void setFeijiDianShuZuComparator(FeijiDianShuZuComparator feijiDianShuZuComparator) {
		this.feijiDianShuZuComparator = feijiDianShuZuComparator;
	}

}
